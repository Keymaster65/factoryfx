package io.github.factoryfx.javafx.editor;

import java.util.ArrayList;

import io.github.factoryfx.javafx.UniformDesignBuilder;
import io.github.factoryfx.javafx.editor.DataEditor;
import io.github.factoryfx.javafx.editor.DataEditorState;
import io.github.factoryfx.javafx.editor.attribute.AttributeVisualisationMappingBuilder;
import io.github.factoryfx.javafx.editor.data.ExampleData1;
import io.github.factoryfx.javafx.editor.data.ExampleData2;
import io.github.factoryfx.javafx.util.UniformDesign;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataEditorTest {

    @Test
    public void testBack() {
        UniformDesign uniformDesign = UniformDesignBuilder.build();
        DataEditor dataEditor = new DataEditor(new AttributeVisualisationMappingBuilder(new ArrayList<>()),uniformDesign);


        ExampleData1 exampleData1 = new ExampleData1();
        ExampleData1 exampleData2 = new ExampleData1();
        ExampleData1 exampleData3 = new ExampleData1();
        ExampleData1 exampleData4 = new ExampleData1();


        dataEditor.navigate(exampleData1);
        dataEditor.navigate(exampleData2);
        dataEditor.navigate(exampleData3);
        dataEditor.navigate(exampleData4);

        dataEditor.back();
        Assertions.assertEquals(exampleData3,dataEditor.editData.get());
        dataEditor.back();
        Assertions.assertEquals(exampleData2,dataEditor.editData.get());
        dataEditor.back();
        Assertions.assertEquals(exampleData1,dataEditor.editData.get());

        dataEditor.back();
        Assertions.assertEquals(exampleData1,dataEditor.editData.get());
        dataEditor.back();
        Assertions.assertEquals(exampleData1,dataEditor.editData.get());
    }

    @Test
    public void testBack_limit() {
        UniformDesign uniformDesign = UniformDesignBuilder.build();
        DataEditor dataEditor = new DataEditor(new AttributeVisualisationMappingBuilder(new ArrayList<>()),uniformDesign);

        for (int i=0;i<100;i++){
            dataEditor.navigate(new ExampleData1());
        }

        Assertions.assertEquals(DataEditorState.HISTORY_LIMIT,dataEditor.dataEditorState.displayedEntities.size());
    }

    @Test
    public void testForward_simple(){
        UniformDesign uniformDesign = UniformDesignBuilder.build();
        DataEditor dataEditor = new DataEditor(new AttributeVisualisationMappingBuilder(new ArrayList<>()),uniformDesign);


        ExampleData1 exampleData1 = new ExampleData1();
        ExampleData1 exampleData2 = new ExampleData1();

        dataEditor.navigate(exampleData1);
        dataEditor.navigate(exampleData2);
        dataEditor.back();
        Assertions.assertEquals(exampleData1, dataEditor.editData.get());
        dataEditor.next();
        Assertions.assertEquals(exampleData2, dataEditor.editData.get());
    }

    @Test
    public void testForward_simple_2(){
        UniformDesign uniformDesign = UniformDesignBuilder.build();
        DataEditor dataEditor = new DataEditor(new AttributeVisualisationMappingBuilder(new ArrayList<>()),uniformDesign);


        ExampleData1 exampleData1 = new ExampleData1();
        ExampleData1 exampleData2 = new ExampleData1();

        dataEditor.navigate(exampleData1);
        dataEditor.navigate(exampleData2);
        dataEditor.back();
        Assertions.assertEquals(exampleData1, dataEditor.editData.get());
        dataEditor.next();
        Assertions.assertEquals(exampleData2, dataEditor.editData.get());
        dataEditor.back();
        Assertions.assertEquals(exampleData1, dataEditor.editData.get());
        dataEditor.next();
        Assertions.assertEquals(exampleData2, dataEditor.editData.get());
    }

    @Test
    public void testForward(){
        UniformDesign uniformDesign = UniformDesignBuilder.build();
        DataEditor dataEditor = new DataEditor(new AttributeVisualisationMappingBuilder(new ArrayList<>()),uniformDesign);


        ExampleData1 exampleData1 = new ExampleData1();
        ExampleData1 exampleData2 = new ExampleData1();
        ExampleData1 exampleData3 = new ExampleData1();
        ExampleData1 exampleData4 = new ExampleData1();


        dataEditor.navigate(exampleData1);
        dataEditor.navigate(exampleData2);
        dataEditor.navigate(exampleData3);
        dataEditor.navigate(exampleData4);


        dataEditor.back();
        Assertions.assertEquals(exampleData3, dataEditor.editData.get());
        dataEditor.back();
        Assertions.assertEquals(exampleData2, dataEditor.editData.get());
        dataEditor.next();
        Assertions.assertEquals(exampleData3,dataEditor.editData.get());
        dataEditor.next();
        Assertions.assertEquals(exampleData4,dataEditor.editData.get());


        dataEditor.next();
        Assertions.assertEquals(exampleData4,dataEditor.editData.get());
    }

    @Test
    public void test_navigated_hierarchy() {
        UniformDesign uniformDesign = UniformDesignBuilder.build();
        DataEditor dataEditor = new DataEditor(new AttributeVisualisationMappingBuilder(new ArrayList<>()),uniformDesign);

        ExampleData1 root = new ExampleData1();
        ExampleData2 value = new ExampleData2();
        root.referenceAttribute.set(value);

        dataEditor.navigate(root);
        dataEditor.navigate(value);

        Assertions.assertEquals(1,dataEditor.dataEditorState.displayedEntities.size());
        Assertions.assertEquals(root,new ArrayList<>(dataEditor.dataEditorState.displayedEntities).get(0));

        dataEditor.navigate(root);
        Assertions.assertEquals(2,dataEditor.dataEditorState.displayedEntities.size());
        Assertions.assertEquals(value,new ArrayList<>(dataEditor.dataEditorState.displayedEntities).get(0));

    }

    @Test
    public void test_navigated_hierarchy2() {
        UniformDesign uniformDesign = UniformDesignBuilder.build();
        DataEditor dataEditor = new DataEditor(new AttributeVisualisationMappingBuilder(new ArrayList<>()),uniformDesign);

        ExampleData1 root = new ExampleData1();
        ExampleData2 value1 = new ExampleData2();
        root.referenceAttribute.set(value1);
        ExampleData2 value2 = new ExampleData2();
        root.referenceListAttribute.add(value2);

        dataEditor.navigate(root);
        dataEditor.navigate(value1);
        dataEditor.back();
        Assertions.assertEquals(0,dataEditor.dataEditorState.displayedEntities.size());
        Assertions.assertEquals(root,dataEditor.editData().get());

        dataEditor.navigate(value2);
        dataEditor.back();
        Assertions.assertEquals(root,dataEditor.editData().get());



    }
}
