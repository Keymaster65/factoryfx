package io.github.factoryfx.data.storage.migration.metadata;

import io.github.factoryfx.data.jackson.ObjectMapperBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeStorageMetadataTest {

    @Test
    public void test_json(){
        AttributeStorageMetadata attributeStorageMetadataTest = new AttributeStorageMetadata("variableName","attributeClassName",true,"referenceClass");
        AttributeStorageMetadata copy  = ObjectMapperBuilder.build().copy(attributeStorageMetadataTest);

        assertEquals("variableName",copy.variableName);
        assertEquals("attributeClassName",copy.attributeClassName);
        assertTrue(copy.isReference);
        assertEquals("referenceClass",copy.referenceClass);

    }

}