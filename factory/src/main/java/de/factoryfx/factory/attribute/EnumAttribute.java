package de.factoryfx.factory.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;

public class EnumAttribute<T extends Enum<T>> extends ValueAttribute<T,EnumAttribute<T>> {

    @JsonCreator
    EnumAttribute(T value) {
        super(null);
        set(value);
    }

    public EnumAttribute(AttributeMetadata attributeMetadata) {
        super(attributeMetadata);
    }
}
