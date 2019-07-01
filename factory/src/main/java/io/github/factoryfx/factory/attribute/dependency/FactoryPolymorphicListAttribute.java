package io.github.factoryfx.factory.attribute.dependency;

import io.github.factoryfx.factory.FactoryBase;
import io.github.factoryfx.factory.PolymorphicFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Attribute for polymorphic Reference.
 * Usually interface with different implementations
 *
 * @param <L> the base interface/class
 */
public class FactoryPolymorphicListAttribute<R extends FactoryBase<?,R>,L> extends FactoryListBaseAttribute<R,L,FactoryBase<? extends L,R>, FactoryPolymorphicListAttribute<R,L>> {


    public FactoryPolymorphicListAttribute() {
        super();
    }

    /**
     * Explanation see: {@link FactoryAttribute#FactoryAttribute(Consumer)}}
     * @param setup setup function
     */
    public FactoryPolymorphicListAttribute(Consumer<FactoryPolymorphicListAttribute<R,L>> setup){
        super();
        setup.accept(this);
    }

    @SafeVarargs
    public FactoryPolymorphicListAttribute(Class<L> liveObjectClass, Class<? extends PolymorphicFactory<?>>... possibleFactoriesClasses) {
        super();
        setup(liveObjectClass,possibleFactoriesClasses);
    }

    private List<Class<?>> possibleFactoriesClasses;


    /**
     * workaround: if possibleFactoriesClasses has generic parameter the normal setup method doesn't work
     * @param liveObjectClass liveObjectClass
     * @param possibleFactoriesClasses possibleFactoriesClasses
     * @return self
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final FactoryPolymorphicListAttribute<R,L> setupUnsafe(Class liveObjectClass, Class... possibleFactoriesClasses){
        this.possibleFactoriesClasses=Arrays.asList(possibleFactoriesClasses);
        for (Class clazz: possibleFactoriesClasses){
            if (!FactoryBase.class.isAssignableFrom(clazz)){
                throw new IllegalArgumentException("parameter must be a factory: "+clazz);
            }
        }
        return setup(liveObjectClass,possibleFactoriesClasses);
    }

    /**
     * setup for select and new value editing
     * @param liveObjectClass type of liveobject
     * @param possibleFactoriesClasses possible factories that crate the liveobject, PolymorphicFactory&lt;L&gt; would be correct but doesn't work
     * @return self
     */
    @SafeVarargs
    public final FactoryPolymorphicListAttribute<R,L> setup(Class<L> liveObjectClass, Class<? extends PolymorphicFactory<?>>... possibleFactoriesClasses){
        this.possibleFactoriesClasses=Arrays.asList(possibleFactoriesClasses);
        new FactoryPolymorphicUtil<R,L>().setup(this,liveObjectClass,()->this.root,possibleFactoriesClasses);
        return this;
    }


    /**
     * intended to be used from code generators
     * @return list of possible classes
     * */
    public List<Class<?>> internal_possibleFactoriesClasses(){
        return possibleFactoriesClasses;
    }


    @SuppressWarnings("unchecked")
    public <T extends FactoryBase> T get(Class<T> clazz) {
        for (FactoryBase<? extends L, R> item : this.get()) {
            if (item.getClass()==clazz){
                return (T)item;
            }
        }
        return null;
    }

    @Override
    public void internal_setReferenceClass(Class<?> clazz) {
        //nothing, reference class is not part of generic parameter
    }

}
