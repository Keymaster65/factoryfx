package de.factoryfx.development.angularjs.server;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.factoryfx.factory.FactoryBase;

public class WebGuiFactoryMetadata {

//    public static class WebGuiAttributeMetadata{
//
//    }

    public String type;

    public Map<String,WebGuiAttributeMetadata> attributes = new HashMap<>();
    public Map<String,String> attributesTypes = new HashMap<>();



    public WebGuiFactoryMetadata(Class<? extends FactoryBase> factoryBaseClass, Locale locale){
        type=factoryBaseClass.getName();
        try {
            FactoryBase<?,?> factoryBase = factoryBaseClass.newInstance();
            factoryBase.visitAttributesFlat((attributeName, attribute) -> {
                attributes.put(attributeName,new WebGuiAttributeMetadata(attribute.metadata,locale));
                attributesTypes.put(attributeName,attribute.getClass().getSimpleName());
            });


        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }



}