package de.factoryfx.factory.attribute;

import java.util.ArrayList;
import java.util.Optional;

import de.factoryfx.factory.FactoryBase;
import de.factoryfx.factory.PreviousLiveObjectProvider;
import de.factoryfx.factory.jackson.ObjectMapperBuilder;
import de.factoryfx.factory.testfactories.ExampleFactoryA;
import de.factoryfx.factory.testfactories.ExampleLiveObjectA;
import org.junit.Assert;
import org.junit.Test;

public class MapAttributeTest {

    public static class ExampleMapFactory extends FactoryBase<ExampleLiveObjectA,ExampleFactoryA> {
        public MapAttribute<Integer,String> mapAttribute=new MapAttribute<>(new AttributeMetadata<>("ExampleA1"));

        @Override
        protected ExampleLiveObjectA createImp(Optional<ExampleLiveObjectA> previousLiveObject, PreviousLiveObjectProvider previousLiveObjectProvider) {
            return null;
        }
    }

    @Test
    public void testObservable(){
        ExampleMapFactory exampleMapFactory = new ExampleMapFactory();
        ArrayList<String> calls= new ArrayList<>();
        exampleMapFactory.mapAttribute.addListener((o)-> {
            calls.add("");
        });
        exampleMapFactory.mapAttribute.get().put(123,"7787");

        Assert.assertEquals(1,calls.size());
    }

    @Test
    public void test_json(){
        ExampleMapFactory exampleMapFactory = new ExampleMapFactory();
        exampleMapFactory.mapAttribute.get().put(123,"7787");
        ObjectMapperBuilder.build().copy(exampleMapFactory);
    }
}