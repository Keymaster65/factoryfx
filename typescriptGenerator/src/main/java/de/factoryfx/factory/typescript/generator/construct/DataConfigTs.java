package de.factoryfx.factory.typescript.generator.construct;

import de.factoryfx.data.Data;
import de.factoryfx.factory.typescript.generator.ts.*;

import java.util.*;

public class DataConfigTs {

    private final Class<? extends Data> clazz;
    private final TsClassFile dataGeneratedTsClass;


    public DataConfigTs(Class<? extends Data> clazz, TsClassFile dataGeneratedTsClass) {
        this.clazz = clazz;
        this.dataGeneratedTsClass = dataGeneratedTsClass;
    }


    public TsClassFile complete(TsClassConstructed tsClass){
        ArrayList<TsMethod> methods = new ArrayList<>();
        methods.add(new TsMethod("initializeAttribute", Collections.emptyList(),new TsMethodResultVoid(),new TsMethodCode(""),"static"));
        tsClass.methods=methods;

        methods.add(new TsMethod("getDisplayText", Collections.emptyList(),new TsMethodResult(new TsTypePrimitive("string")),new TsMethodCode("return '"+splitCamelCase(clazz.getSimpleName())+"';"),"public"));
        tsClass.methods=methods;


        tsClass.addStaticInitialisation("initializeAttribute");
        tsClass.extendsFrom(dataGeneratedTsClass);
        return tsClass;
    }

    static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }

}
