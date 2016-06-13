package de.factoryfx.development.angularjs.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.factoryfx.factory.FactoryBase;

public class WebGuiEntity {

    public static class PathElement{
        public final String id;
        public final String displayText;

        public PathElement(String id, String displayText) {
            this.id = id;
            this.displayText = displayText;
        }
    }


    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    public FactoryBase<?,?> factory;
    public String type;
    public String displayText;
    public Map<String,String> nestedFactoriesDisplayText = new HashMap<>();

    public List<PathElement> path=new ArrayList<>();

    public WebGuiEntity(FactoryBase<?,?> factory, FactoryBase<?,?> root) {
        this.factory = factory.copyOneLevelDeep();
        this.type = factory.getClass().getName();

        factory.visitAttributesFlat((attributeName, attribute) -> {
            attribute.visit(factoryBase1 -> {
                nestedFactoriesDisplayText.put(factoryBase1.getId(), factoryBase1.getDisplayText());
            });
        });

        displayText=factory.getDisplayText();

        for (FactoryBase<?,?> factoryBase: root.getPathTo(factory)){
            path.add(new PathElement(factoryBase.getId(),factoryBase.getDisplayText()));
        }
    }

    public WebGuiEntity() {
        //for jackson

    }
}