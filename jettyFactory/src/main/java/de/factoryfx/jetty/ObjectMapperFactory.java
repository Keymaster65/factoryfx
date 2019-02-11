package de.factoryfx.jetty;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.factoryfx.data.jackson.ObjectMapperBuilder;
import de.factoryfx.factory.FactoryBase;
import de.factoryfx.factory.SimpleFactoryBase;

public class ObjectMapperFactory<V,R extends FactoryBase<?,V,R>> extends SimpleFactoryBase<ObjectMapper,V,R> {
    @Override
    public ObjectMapper createImpl() {
        return ObjectMapperBuilder.buildNewObjectMapper();
    }
}