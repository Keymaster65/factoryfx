package de.factoryfx.factory.datastorage.oracle;

import de.factoryfx.data.Data;
import de.factoryfx.data.merge.MergeDiffInfo;
import de.factoryfx.data.storage.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public class OracledbDataStorage<R extends Data,S> implements DataStorage<R,S> {
    private final OracledbFactoryStorageHistory<R,S> oracledbFactoryStorageHistory;
    private final OracledbFactoryStorageFuture<R,S> oracledbFactoryStorageFuture;
    private final R initialFactory;
    private final DataSerialisationManager<R,S> dataSerialisationManager;
    private final Supplier< Connection > connectionSupplier;
    private final ChangeSummaryCreator<R,S> changeSummaryCreator;

    public OracledbDataStorage(Supplier<Connection> connectionSupplier, R defaultFactory, DataSerialisationManager<R,S> dataSerialisationManager, OracledbFactoryStorageHistory<R,S> oracledbFactoryStorageHistory, OracledbFactoryStorageFuture<R,S> oracledbFactoryStorageFuture, ChangeSummaryCreator<R,S> changeSummaryCreator ){
        this.initialFactory=defaultFactory;
        this.connectionSupplier = connectionSupplier;
        this.dataSerialisationManager = dataSerialisationManager;
        this.oracledbFactoryStorageHistory = oracledbFactoryStorageHistory;
        this.oracledbFactoryStorageFuture = oracledbFactoryStorageFuture;
        this.changeSummaryCreator=changeSummaryCreator;

        try (Connection connection= connectionSupplier.get()){
            try (Statement statement = connection.createStatement()){
                String sql = "CREATE TABLE FACTORY_CURRENT " +
                        "(id VARCHAR(255) not NULL, " +
                        " factory BLOB, " +
                        " factoryMetadata BLOB, " +
                        " PRIMARY KEY ( id ))";

                statement.executeUpdate(sql);
            }

        } catch (SQLException e) {
            //oracle don't know IF NOT EXISTS
            //workaround ignore exception
//            throw new RuntimeException(e);
        }
    }

    public OracledbDataStorage(Supplier<Connection> connectionSupplier, R defaultFactory, DataSerialisationManager<R,S> dataSerialisationManager, OracledbFactoryStorageHistory<R,S> oracledbFactoryStorageHistory, OracledbFactoryStorageFuture<R,S> oracledbFactoryStorageFuture){
        this(
            connectionSupplier,
            defaultFactory,
            dataSerialisationManager,
            oracledbFactoryStorageHistory,
            oracledbFactoryStorageFuture,
            (d)->null
        );

    }
    public OracledbDataStorage(Supplier< Connection > connectionSupplier, R defaultFactory, DataSerialisationManager<R,S> dataSerialisationManager){
        this(connectionSupplier,defaultFactory, dataSerialisationManager,new OracledbFactoryStorageHistory<>(connectionSupplier, dataSerialisationManager),
                new OracledbFactoryStorageFuture<>(connectionSupplier, dataSerialisationManager));
    }

    @Override
    public R getHistoryFactory(String id) {
        return oracledbFactoryStorageHistory.getHistoryFactory(id);
    }

    @Override
    public Collection<StoredDataMetadata<S>> getHistoryFactoryList() {
        return oracledbFactoryStorageHistory.getHistoryFactoryList();
    }

    @Override
    public DataAndStoredMetadata<R,S> getCurrentFactory() {
        try (Connection connection= connectionSupplier.get()){
            try (Statement statement = connection.createStatement()){
                String sql = "SELECT * FROM FACTORY_CURRENT";

                try (ResultSet resultSet =statement.executeQuery(sql)){
                    if(resultSet.next()){
                        StoredDataMetadata<S> factoryMetadata = dataSerialisationManager.readStoredFactoryMetadata(JdbcUtil.readStringToBlob(resultSet,"factoryMetadata"));
                        return new DataAndStoredMetadata<>(dataSerialisationManager.read(JdbcUtil.readStringToBlob(resultSet,"factory"),factoryMetadata.dataModelVersion),factoryMetadata);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void updateCurrentFactory(DataAndNewMetadata<R> update, String user, String comment, MergeDiffInfo<R> mergeDiffInfo) {
        S changeSummary = null;
        if (mergeDiffInfo!=null){
            this.changeSummaryCreator.createChangeSummary(mergeDiffInfo);
        }
        final StoredDataMetadata<S> storedDataMetadata = new StoredDataMetadata<>(
            LocalDateTime.now(),
            UUID.randomUUID().toString(),
            user,
            comment,
            update.metadata.baseVersionId,
            update.metadata.dataModelVersion, changeSummary
        );

        try (Connection connection= connectionSupplier.get()){
            try (PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE TABLE FACTORY_CURRENT")){
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO FACTORY_CURRENT(id,factory,factoryMetadata) VALUES (?,?,? )")){
                preparedStatement.setString(1, storedDataMetadata.id);
                JdbcUtil.writeStringToBlob(dataSerialisationManager.write(update.root),preparedStatement,2);
                JdbcUtil.writeStringToBlob(dataSerialisationManager.writeStorageMetadata(storedDataMetadata),preparedStatement,3);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        oracledbFactoryStorageHistory.updateHistory(storedDataMetadata,update.root);
    }

    @Override
    public DataAndNewMetadata<R> getPrepareNewFactory(){
        NewDataMetadata metadata = new NewDataMetadata();
        metadata.baseVersionId=getCurrentFactory().metadata.id;
        dataSerialisationManager.prepareNewFactoryMetadata(metadata);
        return new DataAndNewMetadata<>(getCurrentFactory().root,metadata);
    }


    @Override
    public void loadInitialFactory() {
        DataAndStoredMetadata<R,S> currentFactory= getCurrentFactory();
        if (currentFactory==null){
            NewDataMetadata metadata = new NewDataMetadata();
            metadata.baseVersionId= UUID.randomUUID().toString();
            DataAndNewMetadata<R> initialFactoryAndStorageMetadata = new DataAndNewMetadata<>(initialFactory,metadata);
            updateCurrentFactory(initialFactoryAndStorageMetadata,"System","initial factory",null);
        }
    }

    @Override
    public Collection<ScheduledDataMetadata<S>> getFutureFactoryList() {
        return oracledbFactoryStorageFuture.getFutureFactoryList();
    }

    @Override
    public void deleteFutureFactory(String id) {
        oracledbFactoryStorageFuture.deleteFutureFactory(id);
    }

    public R getFutureFactory(String id) {
        return oracledbFactoryStorageFuture.getFutureFactory(id);
    }

    @Override
    public ScheduledDataMetadata<S> addFutureFactory(R futureFactory, NewScheduledDataMetadata futureFactoryMetadata, String user, String comment, MergeDiffInfo<R> mergeDiffInfo) {
        final ScheduledDataMetadata<S> storedFactoryMetadata = new ScheduledDataMetadata<>(
            LocalDateTime.now(),
            UUID.randomUUID().toString(),
            user,
            comment,
            futureFactoryMetadata.newDataMetadata.baseVersionId,
            futureFactoryMetadata.newDataMetadata.dataModelVersion,
            this.changeSummaryCreator.createFutureChangeSummary(mergeDiffInfo),
            futureFactoryMetadata.scheduled
        );

        oracledbFactoryStorageFuture.addFuture(storedFactoryMetadata,futureFactory);
        return storedFactoryMetadata;
    }



}