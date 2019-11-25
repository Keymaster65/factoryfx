package io.github.factoryfx.factory.fastfactory;

import io.github.factoryfx.factory.AttributeMetadataVisitor;
import io.github.factoryfx.factory.FactoryBase;
import io.github.factoryfx.factory.attribute.Attribute;
import io.github.factoryfx.factory.attribute.AttributeCopy;
import io.github.factoryfx.factory.attribute.AttributeMatch;
import io.github.factoryfx.factory.attribute.CopySemantic;
import io.github.factoryfx.factory.attribute.dependency.FactoryAttribute;
import io.github.factoryfx.factory.metadata.AttributeMetadata;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FastFactoryAttribute<R extends FactoryBase<?,R>, F extends FactoryBase<?,R>,L,V extends FactoryBase<L,R>> extends FastFactoryAttributeUtility<R,F,V,FactoryAttribute<L,V>>{

    private final Class<V> referenceClass;
    public FastFactoryAttribute(Supplier<FactoryAttribute<L, V>> attributeCreator, Function<F,V> valueGetter, BiConsumer<F,V> valueSetter, Class<V> referenceClass, String attributeName) {
        super(attributeCreator,valueGetter,valueSetter,attributeName);
        this.referenceClass=referenceClass;
    }

    @Override
    protected FactoryAttribute<L, V> getAttribute() {
        return super.getAttribute();
    }

    @Override
    public void visitChildFactory(Consumer<FactoryBase<?,?>> consumer){
        V valueFactory = valueGetter.apply(this.boundFactory);
        if (valueFactory!=null){
            consumer.accept(valueFactory);
        }
    }

    @Override
    public void internal_copyTo(AttributeCopy<V> copyAttribute, final int level, final int maxLevel, final List<FactoryBase<?,?>> oldData, FactoryBase<?,?> parent, FactoryBase<?,?> root){
        V valueFactory = valueGetter.apply(this.boundFactory);
        if (valueFactory!=null){
            V copy = valueFactory.internal().copyDeep(level, maxLevel, oldData, parent, root);
            copyAttribute.set(copy);
        }
    }

    @Override
    public void internal_semanticCopyTo(AttributeCopy<V> copyAttribute) {
        V valueFactory = valueGetter.apply(this.boundFactory);
        if (valueFactory!=null){
            if (getAttribute().internal_getCopySemantic()== CopySemantic.SELF){
                copyAttribute.set(valueFactory);
            } else {
                copyAttribute.set(valueFactory.utility().semanticCopy());
            }
        }

    }

    @Override
    public boolean internal_mergeMatch(AttributeMatch<V> value) {
        V thisValue = valueGetter.apply(this.boundFactory);
        return internal_referenceEquals(thisValue,value.get());
    }

    @Override
    public void internal_merge(V newValue){
        valueSetter.accept(boundFactory,newValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AttributeMetadata createAttributeMetadata() {
        return new AttributeMetadata(this.attributeName, (Class<? extends Attribute<?, ?>>) getAttribute().getClass(),referenceClass,null,getAttribute().internal_getLabelText(),getAttribute().internal_required());
    }
}

