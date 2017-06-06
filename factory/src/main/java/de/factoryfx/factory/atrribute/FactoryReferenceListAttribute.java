package de.factoryfx.factory.atrribute;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import de.factoryfx.data.attribute.Attribute;
import de.factoryfx.data.attribute.AttributeMetadata;
import de.factoryfx.data.attribute.ReferenceListAttribute;
import de.factoryfx.factory.FactoryBase;

public class FactoryReferenceListAttribute<L,T extends FactoryBase<? extends L,?>> extends  ReferenceListAttribute<T,FactoryReferenceListAttribute<L,T>>{

    private Class<T> clazz;
    private AttributeMetadata attributeMetadata;

    @JsonCreator
    protected FactoryReferenceListAttribute() {
        super();
    }

    public FactoryReferenceListAttribute(Class<T> clazz, AttributeMetadata attributeMetadata) {
        super(clazz, attributeMetadata);
        this.clazz=clazz;
        this.attributeMetadata=attributeMetadata;
    }

    //Workaround for generics (T with generic params)
    @SuppressWarnings("unchecked")
    public FactoryReferenceListAttribute(AttributeMetadata attributeMetadata, Class clazz) {
        super(clazz, attributeMetadata);
    }

    public List<L> instances(){
        if (get()==null){
            return null;
        }
        ArrayList<L> result = new ArrayList<>();
        for(T item: get()){
            result.add(item.internalFactory().instance());
        }
        return result;
    }

    public boolean add(T data){
        return get().add(data);
    }


    @Override
    public Attribute<List<T>> internal_copy() {
        return new FactoryReferenceListAttribute(clazz,attributeMetadata);
    }
}
