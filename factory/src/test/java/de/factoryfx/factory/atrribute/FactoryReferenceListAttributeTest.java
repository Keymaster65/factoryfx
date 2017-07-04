package de.factoryfx.factory.atrribute;

import de.factoryfx.factory.testfactories.ExampleFactoryA;
import de.factoryfx.factory.testfactories.ExampleLiveObjectA;
import org.junit.Assert;
import org.junit.Test;


public class FactoryReferenceListAttributeTest {

    @Test
    public void test_filtred_instance(){
        FactoryReferenceListAttribute<ExampleLiveObjectA,ExampleFactoryA> attribute = new FactoryReferenceListAttribute<>();
        {
            ExampleFactoryA data = new ExampleFactoryA();
            data.stringAttribute.set("1");
            attribute.add(data);
        }
        {
            ExampleFactoryA data = new ExampleFactoryA();
            data.stringAttribute.set("2");
            attribute.add(data);
        }
        {
            ExampleFactoryA data = new ExampleFactoryA();
            data.stringAttribute.set("3");
            attribute.add(data);
        }

        ExampleLiveObjectA instance = attribute.instance(exampleFactoryA -> exampleFactoryA.stringAttribute.get().equals("1"));
        Assert.assertNotNull(instance);
    }

}