package de.factoryfx.data.merge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.factoryfx.data.Data;
import de.factoryfx.data.attribute.Attribute;
import de.factoryfx.data.attribute.AttributeVisitor;
import de.factoryfx.data.attribute.ReferenceAttribute;
import de.factoryfx.data.attribute.ReferenceListAttribute;

//just the infotext used in gui
public class AttributeDiffInfo {
    @JsonProperty
    private final String attributeName;
    @JsonProperty
    private final String dataId;

    @JsonCreator
    public AttributeDiffInfo(
            @JsonProperty("attributeName") String attributeName,
            @JsonProperty("parentId") String dataId) {
        this.attributeName = attributeName;
        this.dataId = dataId;
    }


    @JsonIgnore
    public String getAttributeDisplayText(Data root){
        Attribute<?,?> attribute = getAttribute(root);
        if (attribute!=null){
            return attribute.getDisplayText();
        }
        return "empty";
    }

    @JsonIgnore
    public Attribute<?,?> getAttribute(Data root){
        Data data = root.internal().collectChildFactoriesMap().get(dataId);
        if (data!=null) {
            Attribute<?,?>[] result= new Attribute<?,?>[1];
            data.internal().visitAttributesFlat((attributeVariableName, attribute) -> {
                if (attributeVariableName.equals(attributeName)){
                    result[0]=attribute;
                }
            });
            return result[0];
        }
        return null;
    }


    @JsonIgnore
    public boolean isFromFactory(String factoryId){
        return dataId.equals(factoryId);
    }

    @JsonIgnore
    public String parentDisplayText(Data root){
        Data data = root.internal().collectChildFactoriesMap().get(dataId);
        if (data!=null) {
            return data.internal().getDisplayText();
        }
        return "";
    }

}
