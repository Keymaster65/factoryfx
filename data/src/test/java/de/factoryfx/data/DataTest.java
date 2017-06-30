package de.factoryfx.data;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.factoryfx.data.attribute.*;
import de.factoryfx.data.attribute.primitive.IntegerAttribute;
import de.factoryfx.data.attribute.types.ObjectValueAttribute;
import de.factoryfx.data.attribute.types.StringAttribute;
import de.factoryfx.data.attribute.types.StringListAttribute;
import de.factoryfx.data.jackson.ObjectMapperBuilder;
import de.factoryfx.data.jackson.SimpleObjectMapper;
import de.factoryfx.data.merge.DataMerger;
import de.factoryfx.data.merge.MergeDiffInfo;
import de.factoryfx.data.merge.testfactories.ExampleFactoryA;
import de.factoryfx.data.merge.testfactories.ExampleFactoryB;
import de.factoryfx.data.merge.testfactories.ExampleFactoryC;
import de.factoryfx.data.util.LanguageText;
import de.factoryfx.data.validation.AttributeValidation;
import de.factoryfx.data.validation.Validation;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class DataTest {

    @Test
    public void test_visitAttributes(){
        ExampleFactoryA testModel = new ExampleFactoryA();
        testModel.stringAttribute.set("xxxx");
        testModel.referenceAttribute.set(new ExampleFactoryB());

        ArrayList<String> calls = new ArrayList<>();
        testModel.internal().visitAttributesFlat(attribute -> calls.add(attribute.get().toString()));
        Assert.assertEquals(3,calls.size());
        Assert.assertEquals("xxxx",calls.get(0));
    }


    public static class ExampleFactoryThis extends Data {
        ArrayList<String> calls=new ArrayList<>();
        public final StringAttribute stringAttribute= new StringAttribute().labelText("ExampleA1").validation(new Validation<String>() {
            @Override
            public LanguageText getValidationDescription() {
                return null;
            }

            @Override
            public boolean validate(String value) {
                calls.add((String) ExampleFactoryThis.this.getId());
                return false;
            }
        });

        public ExampleFactoryThis(){
            System.out.println();        }
    }


    @Test
    public void test_reconstructMetadataDeepRoot_this() throws IOException {
        ExampleFactoryThis exampleFactoryThis = new ExampleFactoryThis();

        SimpleObjectMapper mapper = ObjectMapperBuilder.build();
        String string = mapper.writeValueAsString(exampleFactoryThis);
        ExampleFactoryThis readed = ObjectMapperBuilder.buildNewObjectMapper().readValue(string,ExampleFactoryThis.class);

        Assert.assertEquals(null,readed.stringAttribute);
        Assert.assertEquals(0,readed.calls.size());

        readed = readed.internal().prepareUsableCopy();

        readed.internal().validateFlat();

        Assert.assertNotNull(readed.stringAttribute);
        Assert.assertEquals(1,readed.calls.size());
        Assert.assertEquals(readed.getId(),readed.calls.get(0));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void test_reconstructMetadataDeepRoot_displaytext() throws Exception {
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        ExampleFactoryB exampleFactoryB = new ExampleFactoryB();
        ExampleFactoryC exampleFactoryC = new ExampleFactoryC();
        exampleFactoryB.referenceAttributeC.set(exampleFactoryC);
        exampleFactoryA.referenceAttribute.set(exampleFactoryB);

        exampleFactoryA.referenceListAttribute.add(new ExampleFactoryB());

        SimpleObjectMapper mapper = ObjectMapperBuilder.build();
        String string = mapper.writeValueAsString(exampleFactoryA);
        ExampleFactoryA readed = ObjectMapperBuilder.buildNewObjectMapper().readValue(string,ExampleFactoryA.class);

        readed = readed.internal().prepareUsableCopy();


        final Field displayTextDependencies = Data.class.getDeclaredField("displayTextDependencies");
        displayTextDependencies.setAccessible(true);
        final List<Attribute<?,?>> attributes = (List<Attribute<?,?>>) displayTextDependencies.get(readed);
        Assert.assertEquals(1, attributes.size());
        Assert.assertEquals(readed.stringAttribute, attributes.get(0));


        final Field dataValidations = Data.class.getDeclaredField("dataValidations");
        displayTextDependencies.setAccessible(true);
        final List<AttributeValidation<?>> dataValidationsAttributes = (List<AttributeValidation<?>>) dataValidations.get(readed);
        Assert.assertEquals(1, dataValidationsAttributes.size());
    }


    @Test
    public void test_getPathFromRoot(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        ExampleFactoryB exampleFactoryB = new ExampleFactoryB();
        exampleFactoryA.referenceAttribute.set(exampleFactoryB);
        exampleFactoryA=exampleFactoryA.internal().prepareUsableCopy();
        Assert.assertNotNull(exampleFactoryA.internal().getRoot());

        List<Data> pathTo = exampleFactoryA.referenceAttribute.get().internal().getPathFromRoot();
        Assert.assertEquals(1,pathTo.size());
        Assert.assertEquals(exampleFactoryA.getId(),pathTo.get(0).getId());
    }


    public static class ExampleObjectProperty extends Data {
        public final StringAttribute stringAttribute= new StringAttribute().labelText("stringAttribute");
        public final ObjectValueAttribute<String> objectValueAttribute= new ObjectValueAttribute<String>().labelText("objectValueAttribute");
    }

    @Test
    public void test_copyOneLevelDeep(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        ExampleFactoryB exampleFactoryB = new ExampleFactoryB();
        exampleFactoryA.referenceAttribute.set(exampleFactoryB);
        ExampleFactoryC exampleFactoryC = new ExampleFactoryC();
        exampleFactoryB.referenceAttributeC.set(exampleFactoryC);

        ExampleFactoryB factoryB = new ExampleFactoryB();
        factoryB.referenceAttributeC.set(new ExampleFactoryC());
        exampleFactoryA.referenceListAttribute.add(factoryB);

        Assert.assertNotNull(exampleFactoryA.referenceAttribute.get());
        Assert.assertNotNull(exampleFactoryA.referenceAttribute.get().referenceAttributeC.get());

        ExampleFactoryA copy =  exampleFactoryA.internal().copyOneLevelDeep();

        Assert.assertNotEquals(copy,exampleFactoryA);
        Assert.assertNotNull(copy.referenceAttribute.get());
        Assert.assertNull(copy.referenceAttribute.get().referenceAttributeC.get());
        Assert.assertNull(copy.referenceListAttribute.get(0).referenceAttributeC.get());
    }

    @Test
    public void test_copyOneLevelDeep_doubleref(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        ExampleFactoryB exampleFactoryB = new ExampleFactoryB();

        exampleFactoryA.referenceAttribute.set(exampleFactoryB);
        exampleFactoryA.referenceListAttribute.add(exampleFactoryB);

        ExampleFactoryA copy =  exampleFactoryA.internal().copyOneLevelDeep();

        Assert.assertEquals(copy.referenceAttribute.get(),copy.referenceListAttribute.get().get(0));
    }

    @Test
    public void test_copy(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        ExampleFactoryB exampleFactoryB = new ExampleFactoryB();
        exampleFactoryB.stringAttribute.set("dfssfdsfdsfd");

        exampleFactoryA.referenceAttribute.set(exampleFactoryB);
        exampleFactoryA.referenceListAttribute.add(new ExampleFactoryB());

        exampleFactoryA = exampleFactoryA.internal().prepareUsableCopy();

        ExampleFactoryA copy =  exampleFactoryA.internal().copy();

        SimpleObjectMapper mapper = ObjectMapperBuilder.build();
        final Data expectedData = mapper.copy(exampleFactoryA);
        String expected = mapper.writeValueAsString(expectedData);
        String actual = mapper.writeValueAsString(copy);
//
//        Assert.assertEquals(expected, actual);
    }

    public static class ExampleWithDefaultParent extends Data {
        public final DataReferenceAttribute<ExampleWithDefault> referenceAttribute = new DataReferenceAttribute<>(ExampleWithDefault.class).labelText("ExampleA2");
    }

    public static class ExampleWithDefault extends Data {
        public final DataReferenceAttribute<ExampleFactoryB> referenceAttribute = new DataReferenceAttribute<>(ExampleFactoryB.class).labelText("ExampleA2").defaultValue(new ExampleFactoryB());
    }
    @Test
    public void test_editing_nested_add(){
        ExampleWithDefaultParent exampleWithDefaultParent = new ExampleWithDefaultParent();
        exampleWithDefaultParent = exampleWithDefaultParent.internal().prepareUsableCopy();
        Assert.assertTrue(exampleWithDefaultParent.internal().readyForUsage());

        List<ExampleWithDefault> exampleWithDefaults = exampleWithDefaultParent.referenceAttribute.internal_createNewPossibleValues();
        ExampleWithDefault exampleWithDefault = exampleWithDefaults.get(0);
        exampleWithDefaultParent.referenceAttribute.set(exampleWithDefault);

        Assert.assertTrue(exampleWithDefaultParent.internal().readyForUsage());
        Assert.assertTrue(exampleWithDefaultParent.referenceAttribute.get().internal().readyForUsage());
        Assert.assertTrue(exampleWithDefaultParent.referenceAttribute.get().referenceAttribute.get().internal().readyForUsage());
    }


    @Test
    public void test_zero_copy(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        exampleFactoryA.stringAttribute.set("dfssfdsfdsfd");
        exampleFactoryA.referenceAttribute.set(new ExampleFactoryB());
        exampleFactoryA.referenceListAttribute.add(new ExampleFactoryB());

        ExampleFactoryA copy =  exampleFactoryA.internal().copyZeroLevelDeep();


        Assert.assertEquals("dfssfdsfdsfd", copy.stringAttribute.get());
        Assert.assertEquals(null, copy.referenceAttribute.get());
        Assert.assertTrue(copy.referenceListAttribute.get().isEmpty());
    }

    private class ExampleFactoryObservable extends Data {
        public final StringAttribute stringAttribute= new StringAttribute().labelText("ExampleA1");
        public ExampleFactoryObservable(){
            config().setDisplayTextProvider(() -> stringAttribute.get());
            config().setDisplayTextDependencies(stringAttribute);
        }
    }

    @Test
    public void test_displaytext_observable(){
        ExampleFactoryObservable exampleFactory = new ExampleFactoryObservable();
        exampleFactory.stringAttribute.set("1");

        Assert.assertEquals("stable ref",exampleFactory.internal().getDisplayTextObservable(),exampleFactory.internal().getDisplayTextObservable());

        Assert.assertEquals("1",exampleFactory.internal().getDisplayTextObservable().get());
        exampleFactory.stringAttribute.set("2");
        Assert.assertEquals("2",exampleFactory.internal().getDisplayTextObservable().get());
    }

    @Test
    public void test_copy_objectProperty(){
        ExampleObjectProperty exampleObjectProperty = new ExampleObjectProperty();
        final String test = "Test";
        exampleObjectProperty.objectValueAttribute.set(test);
        ExampleObjectProperty copy = exampleObjectProperty.internal().copy();
        //that comparision ist correct (no equals)
        Assert.assertTrue(copy.objectValueAttribute.get()==test);
    }


    @Test
    public void test_copy_semantic_self(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        exampleFactoryA.stringAttribute.set("dfssfdsfdsfd");
        exampleFactoryA.referenceAttribute.set(new ExampleFactoryB());
        exampleFactoryA.referenceListAttribute.add(new ExampleFactoryB());

        ExampleFactoryA copy =  exampleFactoryA.utility().semanticCopy();

        Assert.assertEquals(exampleFactoryA.stringAttribute.get(), copy.stringAttribute.get());
        Assert.assertEquals(exampleFactoryA.referenceAttribute.get(), copy.referenceAttribute.get());
        Assert.assertEquals(exampleFactoryA.referenceListAttribute.get(), copy.referenceListAttribute.get());
    }

    @Test
    public void test_copy_semantic_copy(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        exampleFactoryA.stringAttribute.set("dfssfdsfdsfd");
        exampleFactoryA.referenceAttribute.set(new ExampleFactoryB());
        exampleFactoryA.referenceListAttribute.add(new ExampleFactoryB());

        exampleFactoryA.referenceAttribute.setCopySemantic(CopySemantic.COPY);
        exampleFactoryA.referenceListAttribute.setCopySemantic(CopySemantic.COPY);

        ExampleFactoryA copy =  exampleFactoryA.utility().semanticCopy();

        Assert.assertEquals(exampleFactoryA.stringAttribute.get(), copy.stringAttribute.get());
        Assert.assertNotEquals(exampleFactoryA.referenceAttribute.get(), copy.referenceAttribute.get());
        Assert.assertNotEquals(exampleFactoryA.referenceAttribute.get().getId(), copy.referenceAttribute.get().getId());
        Assert.assertNotEquals(exampleFactoryA.referenceListAttribute.get(), copy.referenceListAttribute.get());
        Assert.assertNotEquals(exampleFactoryA.referenceAttribute.get().getId(), copy.referenceAttribute.get().getId());
    }

    @Test
    public void test_copy_semantic_copy_json_id_unique(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        exampleFactoryA.referenceListAttribute.add(new ExampleFactoryB());

        exampleFactoryA.referenceAttribute.setCopySemantic(CopySemantic.COPY);
        exampleFactoryA.referenceListAttribute.setCopySemantic(CopySemantic.COPY);

        ExampleFactoryB copy =  exampleFactoryA.referenceListAttribute.get(0).utility().semanticCopy();
        exampleFactoryA.referenceListAttribute.add(copy);

        ObjectMapperBuilder.build().copy(exampleFactoryA);
    }

    @Test
    public void test_copy_semantic_copy_json_id_unique_2(){
        ExampleFactoryA factoryA = new ExampleFactoryA();
        final ExampleFactoryB factoryB = new ExampleFactoryB();
        factoryB.referenceAttributeC.set(new ExampleFactoryC());
        factoryA.referenceListAttribute.add(factoryB);

        factoryB.referenceAttributeC.setCopySemantic(CopySemantic.SELF);
        factoryA.referenceAttribute.setCopySemantic(CopySemantic.SELF);
        factoryA.referenceListAttribute.setCopySemantic(CopySemantic.SELF);

        ExampleFactoryB copy =  factoryA.referenceListAttribute.get(0).utility().semanticCopy();
        factoryA.referenceListAttribute.add(copy);

        Assert.assertEquals(2, factoryA.referenceListAttribute.size());
        ObjectMapperBuilder.build().copy(factoryA);
    }

    @Ignore
    @Test
    public void test_iterate_performance(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        exampleFactoryA.stringAttribute.set("dfssfdsfdsfd");
        exampleFactoryA.referenceAttribute.set(new ExampleFactoryB());
        exampleFactoryA.referenceListAttribute.add(new ExampleFactoryB());

        int[] forceExecution=new int[]{0};

        final long start = System.currentTimeMillis();
        final Data.AttributeVisitor attributeVisitor = (attributeVariableName, attribute) -> {
            forceExecution[0]++;
        };
        for (int i=0;i<100000000;i++){
            exampleFactoryA.internal().visitAttributesFlat(attributeVisitor);
        }

        System.out.println(forceExecution[0]);
        System.out.println("time: "+(System.currentTimeMillis()-start));

    }

    @Ignore
    @Test
    public void test_copy_performance(){
        ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
        exampleFactoryA.stringAttribute.set("dfssfdsfdsfd");
        exampleFactoryA.referenceAttribute.set(new ExampleFactoryB());
        exampleFactoryA.referenceListAttribute.add(new ExampleFactoryB());

        int[] forceExecution=new int[]{0};

        final long start = System.currentTimeMillis();
        for (int i=0;i<100000;i++){
            final Data copy = exampleFactoryA.internal().copy();
            forceExecution[0]++;
        }
        System.out.println(forceExecution[0]);
        System.out.println("time: "+(System.currentTimeMillis()-start));

    }


}