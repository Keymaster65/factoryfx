package de.factoryfx.server;

import java.util.Collection;

import de.factoryfx.data.merge.DataMerger;
import de.factoryfx.data.merge.MergeDiffInfo;
import de.factoryfx.data.storage.*;
import de.factoryfx.data.storage.migration.GeneralStorageMetadata;
import de.factoryfx.factory.FactoryBase;
import de.factoryfx.factory.FactoryManager;
import de.factoryfx.factory.RootFactoryWrapper;
import de.factoryfx.factory.log.FactoryUpdateLog;

/**
 * starting point for factoryfx application
 *
 * @param <V> Visitor
 * @param <R> Root
 * @param <S> Summary Data for factory history
 */
public class Microservice<V,L,R extends FactoryBase<L,V,R>,S> {
    private final FactoryManager<V,L,R> factoryManager;
    private final DataStorage<R,S> dataStorage;
    private final ChangeSummaryCreator<R,S> changeSummaryCreator;

    public final GeneralStorageMetadata generalStorageMetadata;

    public Microservice(FactoryManager<V,L,R> factoryManager, DataStorage<R,S> dataStorage, ChangeSummaryCreator<R,S> changeSummaryCreator, GeneralStorageMetadata generalStorageMetadata) {
        this.factoryManager = factoryManager;
        this.dataStorage = dataStorage;
        this.changeSummaryCreator = changeSummaryCreator;
        this.generalStorageMetadata = generalStorageMetadata;
    }

    public MergeDiffInfo<R> getDiffToPreviousVersion(StoredDataMetadata<S> storedDataMetadata) {
        R historyFactory = getHistoryFactory(storedDataMetadata.id);
        R historyFactoryPrevious = getPreviousHistoryFactory(storedDataMetadata.id);
        return new DataMerger<>(historyFactoryPrevious,historyFactoryPrevious.utility().copy(),historyFactory).createMergeResult((permission)->true).executeMerge();
    }

    public FactoryUpdateLog<R> revertTo(StoredDataMetadata<S> storedDataMetadata, String user) {
        R historyFactory = getHistoryFactory(storedDataMetadata.id);
        DataAndId<R> currentFactory = dataStorage.getCurrentData();
        return updateCurrentFactory(new DataUpdate<>(
                historyFactory,
                user,
                "revert",
                currentFactory.id)
        );
    }

    public FactoryUpdateLog<R> updateCurrentFactory(DataUpdate<R> update) {
        R commonVersion = dataStorage.getHistoryData(update.baseVersionId);
        FactoryUpdateLog<R> factoryLog = factoryManager.update(commonVersion,update.root, update.permissionChecker);
        if (!factoryLog.failedUpdate() && factoryLog.successfullyMerged()){

            S changeSummary=null;
            if (factoryLog.mergeDiffInfo!=null && changeSummaryCreator!=null){
                changeSummary=changeSummaryCreator.createChangeSummary(factoryLog.mergeDiffInfo);
            }

            R copy = factoryManager.getCurrentFactory().internal().copy();
            DataUpdate<R> updateAfterMerge = new DataUpdate<>(
                    copy,
                    update.user,
                    update.comment,
                    update.baseVersionId
            );
            dataStorage.updateCurrentData(updateAfterMerge,changeSummary);
        }
        return factoryLog;
    }


    public MergeDiffInfo<R> simulateUpdateCurrentFactory(DataUpdate<R> possibleUpdate){
        R commonVersion = dataStorage.getHistoryData(possibleUpdate.baseVersionId);
        return factoryManager.simulateUpdate(commonVersion , possibleUpdate.root, possibleUpdate.permissionChecker);
    }

    /**
     *  prepare a new factory which could be used to update data. mainly give it the correct baseVersionId
     *  @return new possible factory update with prepared ids/metadata
     * */
    public DataUpdate<R> prepareNewFactory() {
        return prepareNewFactory("","");
    }

    /**
     *  prepare a new factory which could be used to update data. mainly give it the correct baseVersionId
     * @param user use
     * @param comment comment
     * @return new possible factory update with prepared ids/metadata
     */
    public DataUpdate<R> prepareNewFactory(String user, String comment) {
        DataAndId<R> currentFactory = dataStorage.getCurrentData();
        DataUpdate<R> update = new DataUpdate<>(
                currentFactory.root.utility().copy(),
                user,
                comment,
                currentFactory.id);
        return update;
    }


    public R getHistoryFactory(String id) {
        return dataStorage.getHistoryData(id);
    }

    private R getPreviousHistoryFactory(String id) {
        return dataStorage.getPreviousHistoryData(id);
    }

    public Collection<StoredDataMetadata<S>> getHistoryFactoryList() {
        return dataStorage.getHistoryDataList();
    }

    public L start() {
        final DataAndId<R> currentFactory = dataStorage.getCurrentData();
        currentFactory.root.internalFactory().setMicroservice(this);//also mind ExceptionResponseAction#reset
        return factoryManager.start(new RootFactoryWrapper<>(currentFactory.root));
    }

    public void stop() {
        factoryManager.stop();
    }

    public V query(V visitor) {
        return factoryManager.query(visitor);
    }

    public L getRootLiveObject(){
        return factoryManager.getCurrentFactory().internalFactory().getLiveObject();
    }
}
