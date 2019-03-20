package io.github.factoryfx.data.storage.migration.datamigration;


import io.github.factoryfx.data.storage.migration.metadata.DataStorageMetadata;
import io.github.factoryfx.data.storage.migration.metadata.DataStorageMetadataDictionary;

public interface AttributePathElement {
    DataJsonNode getNext(DataJsonNode current);
    DataStorageMetadata getNext(DataStorageMetadata current, DataStorageMetadataDictionary dictionary);

    boolean match(AttributePathElement attributePathElement);
}