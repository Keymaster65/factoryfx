package de.factoryfx.javafx.editor.attribute;

import java.util.ArrayList;

import de.factoryfx.data.attribute.AttributeMetadata;
import de.factoryfx.data.attribute.types.StringAttribute;
import de.factoryfx.javafx.UniformDesignBuilder;
import org.junit.Assert;
import org.junit.Test;

public class AttributeEditorBuilderTest {

    @Test
    public void test_canEdit(){
        AttributeEditorBuilder attributeEditor = new AttributeEditorBuilder(UniformDesignBuilder.build(),new ArrayList<>());

        Assert.assertNotNull(attributeEditor.getAttributeEditor(new StringAttribute(new AttributeMetadata()),null,null,null));
    }

}