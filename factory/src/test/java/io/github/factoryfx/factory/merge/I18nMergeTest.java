package io.github.factoryfx.factory.merge;

import java.util.Locale;


import io.github.factoryfx.factory.FactoryBase;
import io.github.factoryfx.factory.attribute.types.I18nAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class I18nMergeTest extends MergeHelperTestBase {

    public static class I18nAttributeTestPojo extends FactoryBase<Void, I18nAttributeTestPojo> {
        public final I18nAttribute attribute=new I18nAttribute();
    }

    @Test
    public void test_merge_change(){
        I18nAttributeTestPojo aTest1 = new I18nAttributeTestPojo();
        aTest1.attribute.en("test1").de("test2");
        aTest1=aTest1.internal().finalise();

        I18nAttributeTestPojo aTest2 = new I18nAttributeTestPojo();
        aTest2.attribute.en("test1X").de("test2X");
        aTest2=aTest2.internal().finalise();

        Assertions.assertTrue(merge(aTest1, aTest1.internal().copy(), aTest2).hasNoConflicts());
        Assertions.assertEquals("test1X",aTest1.attribute.get().internal_getPreferred(Locale.ENGLISH));
        Assertions.assertEquals("test2X",aTest1.attribute.get().internal_getPreferred(Locale.GERMAN));
    }



}