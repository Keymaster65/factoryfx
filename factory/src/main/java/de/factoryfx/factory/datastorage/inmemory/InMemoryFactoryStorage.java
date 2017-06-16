package de.factoryfx.factory.datastorage.inmemory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import de.factoryfx.factory.FactoryBase;
import de.factoryfx.factory.datastorage.FactoryAndNewMetadata;
import de.factoryfx.factory.datastorage.FactoryAndStoredMetadata;
import de.factoryfx.factory.datastorage.FactoryStorage;
import de.factoryfx.factory.datastorage.NewFactoryMetadata;
import de.factoryfx.factory.datastorage.StoredFactoryMetadata;


public class InMemoryFactoryStorage<V,L,T extends FactoryBase<L,V>> implements FactoryStorage<V,L,T> {
    private Map<String,FactoryAndStoredMetadata<T>> storage = new TreeMap<>();
    private String current;
    private T initialFactory;

    public InMemoryFactoryStorage(T initialFactory){
        this.initialFactory=initialFactory;
    }



    @Override
    public T getHistoryFactory(String id) {
        FactoryAndStoredMetadata<T> data = storage.get(id);
        return data.root.internal().copyRoot();
    }

    @Override
    public Collection<StoredFactoryMetadata> getHistoryFactoryList() {
        return storage.values().stream()/*.filter(factory -> !factory.metadata.id.equals(current))*/.map(item -> item.metadata).collect(Collectors.toList());
    }

    @Override
    public FactoryAndStoredMetadata<T> getCurrentFactory() {
        FactoryAndStoredMetadata<T> result = storage.get(current);
        return new FactoryAndStoredMetadata<>(result.root.internal().prepareUsableCopy(),result.metadata);
    }

    @Override
    public void updateCurrentFactory(FactoryAndNewMetadata<T> update, String user, String comment) {
        final StoredFactoryMetadata storedFactoryMetadata = new StoredFactoryMetadata();
        storedFactoryMetadata.creationTime=LocalDateTime.now();
        storedFactoryMetadata.id= UUID.randomUUID().toString();
        storedFactoryMetadata.user=user;
        storedFactoryMetadata.comment=comment;
        storedFactoryMetadata.baseVersionId=update.metadata.baseVersionId;
        storedFactoryMetadata.dataModelVersion=update.metadata.dataModelVersion;

        final FactoryAndStoredMetadata<T> updateData = new FactoryAndStoredMetadata<>(update.root, storedFactoryMetadata);
        storage.put(updateData.metadata.id, updateData);
        current=updateData.metadata.id;
    }

    @Override
    public FactoryAndNewMetadata<T> getPrepareNewFactory(){
        NewFactoryMetadata metadata = new NewFactoryMetadata();
        metadata.baseVersionId=current;
        return new FactoryAndNewMetadata<>(getCurrentFactory().root,metadata);
    }

    @Override
    public void loadInitialFactory() {
        current = UUID.randomUUID().toString();
        StoredFactoryMetadata metadata = new StoredFactoryMetadata();
        metadata.id=current;
        metadata.baseVersionId=current;
        metadata.user="System";
        storage.put(current,new FactoryAndStoredMetadata<>(initialFactory, metadata));
    }
}
