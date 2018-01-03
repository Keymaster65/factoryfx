package de.factoryfx.data.storage;

import de.factoryfx.data.jackson.ObjectMapperBuilder;
import de.factoryfx.data.merge.testfactories.ExampleDataA;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataAndScheduledMetadataTest {

    @Test
    public void test_json(){
        DataAndScheduledMetadata<ExampleDataA> test = new DataAndScheduledMetadata<>(new ExampleDataA(),new ScheduledDataMetadata());
        ObjectMapperBuilder.build().copy(test);
    }

}