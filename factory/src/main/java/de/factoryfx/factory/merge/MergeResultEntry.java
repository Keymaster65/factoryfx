package de.factoryfx.factory.merge;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.factoryfx.factory.FactoryBase;
import de.factoryfx.factory.attribute.Attribute;

public class MergeResultEntry<T extends FactoryBase<?,T>> {
    public final FactoryBase<?,T> parent;
    public final Attribute<?> attribute;
    public final String previousValueDisplayText;
    public final String newValuepValueDisplayText;
    public List<FactoryBase<?,?>> path;

    @JsonCreator
    public MergeResultEntry(
            @JsonProperty("> parent") FactoryBase<?, T> parent,
            @JsonProperty("attribute")Attribute<?> attribute,
            @JsonProperty("previousValueDisplayText")String previousValueDisplayText,
            @JsonProperty("newValuepValueDisplayText")String newValuepValueDisplayText) {
        this.parent=parent;
        this.attribute=attribute;
        this.previousValueDisplayText=previousValueDisplayText;
        this.newValuepValueDisplayText=newValuepValueDisplayText;
    }


//    public String getPathDisplayText() {
//        return path.stream().map(pathElement -> pathElement.getDescriptiveName()).collect(Collectors.joining("/"));
//    }


//    public void setPath(List<FactoryBase<?,?>> path) {
//        this.path = path;
//    }
}