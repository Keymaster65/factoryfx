package de.factoryfx.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.reflect.ClassPath;
import de.factoryfx.factory.LiveObject;
import de.factoryfx.factory.validator.FactoryStyleValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class LiveObjectTest {

    @Parameterized.Parameters(name = "{index}:{0}")
    public static Iterable<Object[]> data1() throws IOException {
        List<Object[]> result = new ArrayList<>();
        for (ClassPath.ClassInfo classInfo: ClassPath.from(LiveObjectTest.class.getClassLoader()).getAllClasses()){
            if (classInfo.getName().startsWith("de.factoryfx")) {
                Class<?> load = classInfo.load();
                if (LiveObject.class.isAssignableFrom(load) && load != LiveObject.class) {
                    result.add(new Object[]{load});
                }
            }
        }

        return result;
    }

    Class<? extends LiveObject> clazz;
    public LiveObjectTest(Class<? extends LiveObject> clazz){
        this.clazz=clazz;
    }

    @Test
    public void test() throws IllegalAccessException, InstantiationException {
        Assert.assertEquals("",new FactoryStyleValidator().validateLiveObject(clazz).orElse(""));
    }
}
