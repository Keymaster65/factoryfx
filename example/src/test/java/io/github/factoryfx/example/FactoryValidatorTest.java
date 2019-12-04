package io.github.factoryfx.example;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import io.github.factoryfx.example.server.OrderMonitoringResourceFactory;
import io.github.factoryfx.factory.FactoryBase;
import io.github.factoryfx.factory.util.ClasspathBasedFactoryProvider;
import io.github.factoryfx.factory.validator.FactoryStyleValidation;
import io.github.factoryfx.factory.validator.FactoryStyleValidatorBuilder;
import io.github.factoryfx.jetty.builder.JettyServerRootFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class FactoryValidatorTest {

    @TestFactory
    List<DynamicTest> createDynamicTests() {
        List<DynamicTest> result = new ArrayList<>();
        final FactoryStyleValidatorBuilder factoryStyleValidatorBuilder = new FactoryStyleValidatorBuilder();
        for (Class<? extends FactoryBase<?,?>> clazz: new ClasspathBasedFactoryProvider().get(OrderMonitoringResourceFactory.class)){
            if (!Modifier.isAbstract( clazz.getModifiers() )){
                final List<FactoryStyleValidation> factoryValidations = factoryStyleValidatorBuilder.createFactoryValidations(clazz);
                for (FactoryStyleValidation factoryStyleValidation: factoryValidations){

                    result.add(DynamicTest.dynamicTest(clazz.getName()+":"+factoryStyleValidation.getClass().getSimpleName(),
                            () -> Assertions.assertEquals("",factoryStyleValidation.validateFactory().orElse(""))));
                }
            }

//            result.add(new Object[]{clazz});
        }


        return result;
    }

}
