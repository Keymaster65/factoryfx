package io.github.factoryfx.factory.attribute.types;

import io.github.factoryfx.factory.jackson.ObjectMapperBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class URIListAttributeTest {

    @Test
    public void test_json(){
        URIListAttribute attribute = new URIListAttribute();
        attribute.addUnchecked("www.google.de");
        URIListAttribute copy = ObjectMapperBuilder.build().copy(attribute);
        Assertions.assertEquals("www.google.de",copy.get().iterator().next().toString());
    }

}