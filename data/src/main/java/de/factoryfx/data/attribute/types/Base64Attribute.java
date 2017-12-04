package de.factoryfx.data.attribute.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import de.factoryfx.data.attribute.ImmutableValueAttribute;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * bytearray as base64 encoded
 */
public class Base64Attribute extends ImmutableValueAttribute<String,Base64Attribute> {
    public Base64Attribute() {
        super(String.class);
    }

    @JsonCreator
    Base64Attribute(String value) {
        super(null);
        set(value);
    }

    @Override
    public String getDisplayText() {
        if (get()!=null){
            return get();
        }
        return "<empty>";
    }

    public void set(byte[] bytes){
        set(Base64.getEncoder().encodeToString(bytes));
    }

    public byte[] getBytes(){
        return Base64.getDecoder().decode(get());
    }

    @Override
    public boolean internal_mergeMatch(String value) {
        return Objects.equals(get(), value);
    }

}
