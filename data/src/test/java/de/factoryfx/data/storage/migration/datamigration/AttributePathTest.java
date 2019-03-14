package de.factoryfx.data.storage.migration.datamigration;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.factoryfx.data.jackson.ObjectMapperBuilder;
import de.factoryfx.data.merge.testdata.ExampleDataA;
import de.factoryfx.data.merge.testdata.ExampleDataB;
import de.factoryfx.data.storage.migration.metadata.DataStorageMetadataDictionary;
import de.factoryfx.data.storage.migration.metadata.ExampleDataAPrevious;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttributePathTest {

    @Test
    public void test_resolve_string(){
        ExampleDataA exampleDataA = new ExampleDataA();
        ExampleDataB exampleDataB = new ExampleDataB();
        exampleDataB.stringAttribute.set("1234");
        exampleDataA.referenceAttribute.set(exampleDataB);

        Assertions.assertEquals("1234",PathBuilder.value(String.class).pathElement("referenceAttribute").attribute("stringAttribute").resolveAttributeValue(new DataJsonNode((ObjectNode)ObjectMapperBuilder.build().writeValueAsTree(exampleDataA)),ObjectMapperBuilder.build()));
    }

    @Test
    public void test_resolve_string_root_attribute(){
        ExampleDataA exampleDataA = new ExampleDataA();
        exampleDataA.stringAttribute.set("1234");
        Assertions.assertEquals("1234",PathBuilder.value(String.class).attribute("stringAttribute").resolveAttributeValue(new DataJsonNode((ObjectNode)ObjectMapperBuilder.build().writeValueAsTree(exampleDataA)),ObjectMapperBuilder.build()));
    }

    @Test
    public void test_resolve_ref(){
        ExampleDataA exampleDataA = new ExampleDataA();
        ExampleDataB exampleDataB = new ExampleDataB();
        exampleDataB.stringAttribute.set("1234");
        exampleDataA.referenceAttribute.set(exampleDataB);


        ExampleDataB referenceAttribute = PathBuilder.value(ExampleDataB.class).attribute("referenceAttribute").resolveAttributeValue(new DataJsonNode((ObjectNode) ObjectMapperBuilder.build().writeValueAsTree(exampleDataA)),ObjectMapperBuilder.build());
        Assertions.assertEquals("1234", referenceAttribute.stringAttribute.get());
    }

    @Test
    public void test_resolve_to_null(){
        ExampleDataA exampleDataA = new ExampleDataA();
        exampleDataA.referenceAttribute.set(null);


        ExampleDataB referenceAttribute = PathBuilder.value(ExampleDataB.class).attribute("referenceAttribute").resolveAttributeValue(new DataJsonNode((ObjectNode) ObjectMapperBuilder.build().writeValueAsTree(exampleDataA)),ObjectMapperBuilder.build());
        assertNull(referenceAttribute);
    }



    @Test
    public void test_resolve_data_ref_id(){
        ExampleDataA exampleDataA = new ExampleDataA();
        exampleDataA.stringAttribute.set("1234");

        ExampleDataB exampleDataB = new ExampleDataB();
        exampleDataA.referenceAttribute.set(exampleDataB);
        exampleDataB.referenceAttribute.set(exampleDataA);


        DataJsonNode root = new DataJsonNode((ObjectNode) ObjectMapperBuilder.build().writeValueAsTree(exampleDataA));


        ExampleDataA referenceAttribute = PathBuilder.value(ExampleDataA.class).pathElement("referenceAttribute").attribute("referenceAttribute").resolveAttributeValue(root,ObjectMapperBuilder.build());
        Assertions.assertEquals("1234", referenceAttribute.stringAttribute.get());
    }


    @Test
    public void test_remove_path_check(){
        ExampleDataAPrevious root = new ExampleDataAPrevious();
        root.stringAttribute.set("1234");

        root.internal().addBackReferences();
        DataStorageMetadataDictionary dictionary = root.internal().createDataStorageMetadataDictionaryFromRoot();
        dictionary.renameClass("de.factoryfx.data.storage.migration.metadata.ExampleDataAPrevious",ExampleDataA.class.getName());

        dictionary.markRemovedAttributes();

        Assertions.assertTrue(PathBuilder.value(ExampleDataA.class).attribute("garbage").isPathToRemovedAttribute(dictionary));
    }

    @Test
    public void test_markRemovedAttributes_removedClass(){
        ExampleDataAPrevious root = new ExampleDataAPrevious();
        root.stringAttribute.set("1234");

        root.internal().addBackReferences();
        DataStorageMetadataDictionary dictionary = root.internal().createDataStorageMetadataDictionaryFromRoot();
        dictionary.renameClass("de.factoryfx.data.storage.migration.metadata.ExampleDataAPrevious","a.b.c.Removed");

        dictionary.markRemovedAttributes();

        Assertions.assertTrue(PathBuilder.value(ExampleDataA.class).attribute("garbage").isPathToRemovedAttribute(dictionary));
    }

    @Test
    public void test_resolve_reflist_string(){
        ExampleDataA exampleDataA = new ExampleDataA();
        ExampleDataB exampleDataB = new ExampleDataB();
        exampleDataB.stringAttribute.set("1234");
        exampleDataA.referenceListAttribute.add(new ExampleDataB());
        exampleDataA.referenceListAttribute.add(exampleDataB);

        Assertions.assertEquals("1234",PathBuilder.value(String.class).pathElement("referenceListAttribute",1).attribute("stringAttribute").resolveAttributeValue(new DataJsonNode((ObjectNode)ObjectMapperBuilder.build().writeValueAsTree(exampleDataA)),ObjectMapperBuilder.build()));
    }


}