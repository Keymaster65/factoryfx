package io.github.factoryfx.factory.builder;

import io.github.factoryfx.data.jackson.SimpleObjectMapper;
import io.github.factoryfx.data.storage.DataStorage;
import io.github.factoryfx.data.storage.migration.MigrationManager;
import io.github.factoryfx.factory.FactoryBase;

@FunctionalInterface
public interface DataStorageCreator<R extends FactoryBase<?,R>,S> {
    DataStorage<R,S> createDataStorage(R initialFactory, MigrationManager<R,S> migrationManager, SimpleObjectMapper objectMapper);
}