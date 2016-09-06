package de.factoryfx.adminui.angularjs.model;

import de.factoryfx.adminui.angularjs.integration.example.ExampleEnum;
import org.junit.Assert;
import org.junit.Test;

public class LayoutDataTypeTest {

    @Test
    public void test_enum(){
        WebGuiDataType dataType = new WebGuiDataType(ExampleEnum.class);
        Assert.assertEquals("Enum",dataType.dataType);
        Assert.assertEquals(3,dataType.enumValues.size());
    }

}