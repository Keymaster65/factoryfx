package de.factoryfx.factory.builder;

import de.factoryfx.factory.SimpleFactoryBase;
import de.factoryfx.factory.parametrized.ParametrizedObjectCreatorAttribute;
import de.factoryfx.factory.parametrized.ParametrizedObjectCreatorFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Function;

public class DefaultCreatorTest {

    public static class DummyCreatorFactory extends ParametrizedObjectCreatorFactory<Void,Void,Void,DummyCreatorFactory> {
        @Override
        protected Function<Void, Void> getCreator() {
            return p -> null;
        }
    }


    public static class ParametrizedTestFactory extends SimpleFactoryBase<Void,Void, ParametrizedTestFactory> {
        public final ParametrizedObjectCreatorAttribute<Void,Void,DummyCreatorFactory> dummyCreator =new ParametrizedObjectCreatorAttribute<>(DummyCreatorFactory.class);

        @Override
        public Void createImpl() {
            return null;
        }
    }

    @Test
    public void test_ParametrizedObjectCreatorAttribute(){
        DefaultCreator<ParametrizedTestFactory,ParametrizedTestFactory> defaultCreator = new DefaultCreator<>(ParametrizedTestFactory.class);

        FactoryContext mock = Mockito.mock(FactoryContext.class);
        ParametrizedTestFactory value = new ParametrizedTestFactory();
        Mockito.when(mock.get(Mockito.any(Class.class))).thenReturn(value);

        ParametrizedTestFactory factory= defaultCreator.apply(mock);

        Assert.assertNotNull(factory.dummyCreator.get());
        Assert.assertEquals(value,factory.dummyCreator.get());
    }


}