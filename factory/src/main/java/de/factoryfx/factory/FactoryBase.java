package de.factoryfx.factory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.factoryfx.factory.attribute.Attribute;
import de.factoryfx.factory.attribute.IterableAttributes;
import de.factoryfx.factory.attribute.ReferenceAttribute;
import de.factoryfx.factory.attribute.ReferenceListAttribute;
import de.factoryfx.factory.jackson.ObjectMapperBuilder;
import de.factoryfx.factory.merge.MergeResult;
import de.factoryfx.factory.merge.MergeResultEntry;
import de.factoryfx.factory.merge.attribute.AttributeMergeHelper;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class FactoryBase<E extends LiveObject, T extends FactoryBase<E,T>> implements IterableAttributes {



    public interface AttributeVisitor{
        void value(Attribute<?> value);
        void reference(ReferenceAttribute<?> reference);
        void referenceList(ReferenceListAttribute<?> referenceList);
    }

    @FunctionalInterface
    interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    public void visitAttributesFlat(AttributeVisitor visitor ) {
        Field[] fields = getFields();
        for (Field field : fields) {
            try {
                Object fieldValue = field.get(this);
                if (fieldValue instanceof Attribute) {
                    Attribute attribute = (Attribute) fieldValue;

                    if (attribute instanceof ReferenceListAttribute) {
                        visitor.referenceList((ReferenceListAttribute<?>) attribute);
                    } else {
                        if (attribute instanceof ReferenceAttribute<?>) {
                            visitor.reference((ReferenceAttribute<?>) attribute);
                        }  else {
                            visitor.value(attribute);
                        }
                    }




                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void visitAttributesDualFlat(Object modelBase, BiConsumer<Attribute<?>, Attribute<?>> consumer) {
        Field[] fields = getFields();
        for (Field field : fields) {
            try {
                Object fieldValue = field.get(this);
                if (fieldValue instanceof Attribute) {
                    consumer.accept((Attribute<?>) field.get(this), (Attribute<?>) field.get(modelBase));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void visitAttributesFlat(Consumer<Attribute<?>> consumer) {
        Field[] fields = getFields();
        for (Field field : fields) {
            try {
                Object fieldValue = field.get(this);
                if (fieldValue instanceof Attribute) {
                    consumer.accept((Attribute) fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void visitAttributesTripleFlat(Optional<?> modelBase1, Optional<?> modelBase2, TriConsumer<Attribute<?>, Optional<Attribute<?>>, Optional<Attribute<?>>> consumer) {
        Field[] fields = getFields();
        for (Field field : fields) {
            try {
//                fields[f].setAccessible(true);
                Object fieldValue = field.get(this);
                if (fieldValue instanceof Attribute) {

                    Attribute<?> value1 = null;
                    if (modelBase1.isPresent()) {
                        value1 = (Attribute<?>) field.get(modelBase1.get());
                    }
                    Attribute<?> value2 = null;
                    if (modelBase2.isPresent()) {
                        value2 = (Attribute<?>) field.get(modelBase2.get());
                    }
                    consumer.accept((Attribute<?>) field.get(this), Optional.ofNullable(value1), Optional.ofNullable(value2));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }










    public void collectModelEntitiesTo(Set<FactoryBase<?,?>> allModelEntities) {
        allModelEntities.add(this);
        visitAttributesFlat(attribute -> attribute.collectChildren(allModelEntities));
    }

    @SuppressWarnings("unchecked")
    public T copy() {
        return ObjectMapperBuilder.build().copy(this).reconstructMetadataDeepRoot();
    }

    @SuppressWarnings("unchecked")
    public void fixDuplicateObjects(Function<String, Optional<FactoryBase<?,?>>> getCurrentEntity) {
        visitAttributesFlat(attribute -> attribute.fixDuplicateObjects(getCurrentEntity));
    }

    public HashMap<FactoryBase<?,?>, FactoryBase<?,?>> getChildToParentMap(Set<FactoryBase<?,?>> allModelEntities) {
        HashMap<FactoryBase<?,?>, FactoryBase<?,?>> result = new HashMap<>();
        for (FactoryBase<?,?> factoryBase : allModelEntities) {
            factoryBase.visitAttributesFlat(attribute -> {
                if (attribute instanceof ReferenceListAttribute) {
                    for (FactoryBase<?,?> factoryBaseRef : ((ReferenceListAttribute<?>) attribute).get()) {
                        result.put(factoryBaseRef, factoryBase);
                    }
                }
                if (attribute instanceof ReferenceAttribute<?>) {
                    result.put(((ReferenceAttribute<?>) attribute).get(), factoryBase);
                }
            });
        }
        return result;
    }


    @JsonIgnore
    public String getDescriptiveName() {
        return getClass().getSimpleName();
    }

    public List<FactoryBase<?,?>> getMassPathTo(HashMap<FactoryBase<?,?>, FactoryBase<?,?>> childToParent, FactoryBase<?,?> target) {
        List<FactoryBase<?,?>> path = new ArrayList<>();
        Optional<FactoryBase<?,?>> pathElement = Optional.ofNullable(childToParent.get(target));
        while (pathElement.isPresent()) {
            path.add(pathElement.get());
            pathElement = Optional.ofNullable(childToParent.get(pathElement.get()));
        }
        Collections.reverse(path);
        return path;
    }

    public Optional<FactoryBase<?,?>> getParent(Set<FactoryBase<?,?>> allModelEntities, FactoryBase child) {
        return Optional.ofNullable(getChildToParentMap(allModelEntities).get(child));
    }

    /**Slow. for multiple calls use getMassPathTo*/
    public List<FactoryBase<?,?>> getPathTo(FactoryBase<?,?> target) {
        HashSet<FactoryBase<?,?>> allModelEntities = new HashSet<>();
        collectModelEntitiesTo(allModelEntities);
        return getMassPathTo(getChildToParentMap(allModelEntities), target);
    }

    @SuppressWarnings("unchecked")
    public void merge(Optional<FactoryBase<?,T>> originalValue, Optional<FactoryBase<?,T>> newValue, MergeResult mergeResult) {

        this.visitAttributesTripleFlat(originalValue, newValue, (currentAttribute, originalAttribute, newAttribute) -> {
            AttributeMergeHelper<?> attributeMergeHelper = currentAttribute.createMergeHelper();
            boolean mergeable = attributeMergeHelper.isMergeable(originalAttribute, newAttribute);
            MergeResultEntry<T> mergeResultEntry = new MergeResultEntry<>((T) FactoryBase.this, (Optional<T>) newValue, Optional.ofNullable(currentAttribute), newAttribute);
            if (mergeable) {
                if (newAttribute.isPresent()) {
                    if (!attributeMergeHelper.equalValues(newAttribute.get())) {
                        mergeResult.addMergeExecutions(() -> attributeMergeHelper.merge(originalAttribute, newAttribute.get()));
                        mergeResult.addMergeInfo(mergeResultEntry);
                    }
                }
            } else {
                mergeResult.addConflictInfos(mergeResultEntry);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private T newInstance() {
        try {
            return (T) getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * after deserialization from json only the value is present and metadata are missing
     * we create a copy which contains the metadata and than copy the values into the copy
     */
    @SuppressWarnings("unchecked")
    public T reconstructMetadataDeep(HashMap<String, FactoryBase<?,?>> objectPool) {
        T copy = (T) objectPool.get(this.getId());
        if (copy == null) {
            copy = newInstance();
            copy.setId(this.getId());
            objectPool.put(copy.getId(), copy);
            visitAttributesDualFlat(copy, (thisAttribute, copyAttribute) -> copyAttribute.setFromValueOnlyAttribute(thisAttribute.get(), objectPool));
        }
        return copy;
    }

    public T reconstructMetadataDeepRoot() {
        return reconstructMetadataDeep(new HashMap<>());
    }

    @JsonIgnore
    static final Map<Class<?>, Field[]> fields = new HashMap<>();
    @JsonIgnore
    Field[] instanceFields;
    private String id;

    public FactoryBase() {
        initFieldsCache();
    }


    public Field[] getFields() {
        return instanceFields;
    }


    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    private void initFieldsCache() {
        synchronized (fields) {
            Field[] f = fields.get(getClass());
            if (f == null) {
                f = getClass().getFields();
                ArrayList<Field> removeStatic = new ArrayList<>();
                for (Field ff : f) {
                    if (!Modifier.isStatic(ff.getModifiers())) {
                        removeStatic.add(ff);
                    }
                }
                fields.put(getClass(), removeStatic.toArray(new Field[removeStatic.size()]));
            }
            instanceFields = f;
        }
    }



    private E createdLiveObjects;
    LoopProtector loopProtector = new LoopProtector();
    public E create(PreviousLiveObjectProvider previousLiveObjectProvider){
        loopProtector.enter();
        try {
            Optional<E> previousLiveObject = previousLiveObjectProvider.get(this);
            E liveObject = createImp(previousLiveObject,previousLiveObjectProvider);
            createdLiveObjects=liveObject;
            return liveObject;
        } finally {
            loopProtector.exit();
        }
    }

    protected abstract E createImp(Optional<E> previousLiveObject, PreviousLiveObjectProvider previousLiveObjectProvider);

    public void collectLiveObjects(Map<String,LiveObject> liveObjects){
        loopProtector.enter();
        try {//order important deep first
            this.visitAttributesFlat(new AttributeVisitor() {
                @Override
                public void value(Attribute<?> value) {
                    //nothing
                }

                @Override
                public void reference(ReferenceAttribute<?> reference) {
                    FactoryBase<LiveObject, ?> factory = (FactoryBase<LiveObject, ?>) reference.get();
                    if (factory!=null){
                        factory.collectLiveObjects(liveObjects);
                    }
                }

                @Override
                public void referenceList(ReferenceListAttribute<?> referenceList) {
                    referenceList.get().forEach((Consumer<FactoryBase<?, ?>>) factory -> {
                        factory.collectLiveObjects(liveObjects);
                    });
                }
            });
            if (createdLiveObjects!=null){
                liveObjects.put(getId(),createdLiveObjects);
            }
        } finally {
            loopProtector.exit();
        }
    }

}
