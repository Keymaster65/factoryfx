package io.github.factoryfx.factory.log;

import io.github.factoryfx.factory.jackson.ObjectMapperBuilder;
import io.github.factoryfx.factory.merge.DataMerger;
import io.github.factoryfx.factory.testfactories.ExampleFactoryA;
import org.junit.jupiter.api.Test;

public class FactoryUpdateLogTest {

    @Test
    public void test_json(){
        DataMerger<ExampleFactoryA> dataMerger = new DataMerger<>(new ExampleFactoryA(),new ExampleFactoryA(),new ExampleFactoryA());

        FactoryUpdateLog<ExampleFactoryA> factoryUpdateLog = new FactoryUpdateLog<>("log",
                        dataMerger.mergeIntoCurrent(p->true),0,null);
        ObjectMapperBuilder.build().copy(factoryUpdateLog);
    }

}