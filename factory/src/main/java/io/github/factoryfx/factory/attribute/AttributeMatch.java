package io.github.factoryfx.factory.attribute;

import io.github.factoryfx.factory.FactoryBase;

import java.util.List;

public interface AttributeMatch<V> extends AttributeValue<V> {
    /**
     * alternative to equals on value, type-safe , less verbose, without worrying about hidden contracts
     * indented for merge some with some serial case e.g ObjectValueAttribute
     * @param value compare value
     * @return true if equals
     */
    boolean internal_mergeMatch(AttributeMatch<V> value);

    default boolean internal_referenceEquals(FactoryBase<?,?> value, FactoryBase<?,?> otherValue) {
        if (otherValue == null && value == null) {
            return true;
        }
        if (otherValue == null || value == null) {
            return false;
        }
        return otherValue.idEquals(value);
    }

    default boolean internal_referenceListEquals(List<? extends FactoryBase<?,?> > value, List<? extends FactoryBase<?,?> > newList) {
        if (value==null ){
            return false;
        }
        if (newList.size() != value.size()) {
            return false;
        }

//        if (value.size() < newList.size()) {
//            for (int i = 0; i < value.size(); i++) {
//                if (!this.internal_referenceEquals(newList.get(i), value.get(i))) {
//                    return false;
//                }
//            }
//        }
        for (int i = 0; i < value.size(); i++) {
            if (!this.internal_referenceEquals(newList.get(i), value.get(i))) {
                return false;
            }
        }
        return true;
    }
}
