package io.github.factoryfx.factory.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.factoryfx.factory.FactoryBase;


public class DataAndId<T extends FactoryBase<?,?>> {
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    public final T root;
    public final String id;

    @JsonCreator
    public DataAndId(@JsonProperty("root") T root, @JsonProperty("id")String id) {
        this.root = root;
        this.id = id;
    }
}
