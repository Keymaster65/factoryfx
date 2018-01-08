package de.factoryfx.javafx.view.factoryviewmanager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.factoryfx.data.jackson.ObjectMapperBuilder;
import de.factoryfx.data.merge.AttributeDiffInfo;
import de.factoryfx.data.merge.MergeDiffInfo;
import de.factoryfx.factory.FactoryBase;
import de.factoryfx.data.storage.DataAndNewMetadata;
import de.factoryfx.data.storage.DataSerialisationManager;
import de.factoryfx.data.storage.NewDataMetadata;
import de.factoryfx.factory.log.FactoryUpdateLog;
import de.factoryfx.server.rest.client.ApplicationServerRestClient;
import javafx.application.Platform;

public class FactoryEditManager<V,R extends FactoryBase<?,V>,S> {
    private final ApplicationServerRestClient<V,R> client;
    private final List<FactoryRootChangeListener<R>> listeners= new ArrayList<>();
    private final DataSerialisationManager<R,S> dataSerialisationManager;

    public FactoryEditManager(ApplicationServerRestClient<V, R> client, DataSerialisationManager<R,S> dataSerialisationManager) {
        this.client = client;
        this.dataSerialisationManager = dataSerialisationManager;
    }

    public List<AttributeDiffInfo> getSingleFactoryHistory(String id) {
        return client.getSingleFactoryHistory(id);
    }

    public void registerListener(FactoryRootChangeListener<R> listener){
        listeners.add(listener);
    }

    public void removeListener(FactoryRootChangeListener listener){
        listeners.remove(listener);
    }

    Optional<DataAndNewMetadata<R>> loadedRoot = Optional.empty();
    public void load(){
        DataAndNewMetadata<R> currentFactory = client.prepareNewFactory();
        Optional<R> previousRoot=getLoadedFactory();
        loadedRoot = Optional.of(currentFactory);

        updateNotify(currentFactory, previousRoot);
    }

    private void updateNotify(DataAndNewMetadata<R> currentFactory, Optional<R> previousRoot) {
        runLaterExecuter.accept(() -> {
            for (FactoryRootChangeListener<R> listener: listeners){
                listener.update(previousRoot,currentFactory.root);
            }
        });
    }

    //for testablility, avoid Toolkit not initialized
    Consumer<Runnable> runLaterExecuter= Platform::runLater;


    public Optional<R> getLoadedFactory(){
        return loadedRoot.map(rFactoryAndNewMetadata -> rFactoryAndNewMetadata.root);
    }

    public void reset() {
        load();
    }

    public FactoryUpdateLog save(String comment) {
        final DataAndNewMetadata<R> update = loadedRoot.get();
        final FactoryUpdateLog factoryLog = client.updateCurrentFactory(update,comment);
        if (factoryLog.mergeDiffInfo.successfullyMerged()) {
            load();//to edit the newly merged data
        }
        return factoryLog;
    }

    public void saveToFile(Path target) {
        FactoryAndStringifyedStorageMetadata factoryAndStringifyedStorageMetadata = new FactoryAndStringifyedStorageMetadata();
        factoryAndStringifyedStorageMetadata.metadata=loadedRoot.get().metadata;
        factoryAndStringifyedStorageMetadata.root= ObjectMapperBuilder.build().writeValueAsString(loadedRoot.get().root);
        ObjectMapperBuilder.build().writeValue(target.toFile(),factoryAndStringifyedStorageMetadata);
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(Path target) {
        Optional<R> previousRoot=getLoadedFactory();
        final FactoryAndStringifyedStorageMetadata value = ObjectMapperBuilder.build().readValue(target.toFile(), FactoryAndStringifyedStorageMetadata.class);
        R serverFactory = dataSerialisationManager.read(value.root, value.metadata.dataModelVersion);
        loadedRoot=Optional.of(new DataAndNewMetadata<>(serverFactory,value.metadata));
        updateNotify(loadedRoot.get(), previousRoot);
    }

    private static class FactoryAndStringifyedStorageMetadata{
        public String root;
        public NewDataMetadata metadata;
    }

    public MergeDiffInfo<R> simulateUpdateCurrentFactory() {
        final DataAndNewMetadata<R> update = loadedRoot.get();
        return client.simulateUpdateCurrentFactory(update);
    }
}
