package de.factoryfx.data.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.factoryfx.data.Data;
import de.factoryfx.data.jackson.ObservableListJacksonAbleWrapper;
import de.factoryfx.data.merge.attribute.AttributeMergeHelper;
import de.factoryfx.data.merge.attribute.ReferenceListMergeHelper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ReferenceListAttribute<T extends Data> extends Attribute<ObservableList<T>> {
    private Data root;

    ObservableList<T> list = FXCollections.observableArrayList();
    private Class<T> clazz;

    public ReferenceListAttribute(Class<T> clazz, AttributeMetadata attributeMetadata) {
        super(attributeMetadata);
        this.clazz=clazz;
    }

    @JsonCreator
    protected ReferenceListAttribute() {
        super(null);
    }

    @JsonCreator
    protected ReferenceListAttribute(ObservableListJacksonAbleWrapper<T> list) {
        super(null);
        this.list = list.unwrap();
    }

    public boolean add(T value) {
        get().add(value);
        return false;
    }


    @Override
    public void collectChildren(Set<Data> allModelEntities) {
        list.forEach(entity -> entity.internal().collectModelEntitiesTo(allModelEntities));
    }

    @Override
    public AttributeMergeHelper<?> createMergeHelper() {
        return new ReferenceListMergeHelper<>(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fixDuplicateObjects(Function<Object, Optional<Data>> getCurrentEntity) {
        List<T> currentToEditList = get();

        for (T entity : currentToEditList) {
            entity.internal().fixDuplicateObjects(getCurrentEntity);
        }

        List<T> fixedList = new ArrayList<>();
        for (T entity : currentToEditList) {
            Optional<Data> existingOptional = getCurrentEntity.apply(entity.getId());
            if (existingOptional.isPresent()) {
                fixedList.add((T) existingOptional.get());
            } else {
                fixedList.add(entity);
            }
        }
        currentToEditList.clear();
        currentToEditList.addAll(fixedList);

    }

    @Override
    public ObservableList<T> get() {
        return list;
    }



    @Override
    public void set(ObservableList<T> value) {
        this.list = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void copyTo(Attribute<ObservableList<T>> copyAttribute, Function<Data,Data> dataCopyProvider) {
        for (T item: get()){
            final T itemCopy = (T) dataCopyProvider.apply(item);
            if (itemCopy!=null){
                copyAttribute.get().add(itemCopy);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void semanticCopyTo(Attribute<ObservableList<T>> copyAttribute, Function<Data,Data> dataCopyProvider) {
        if (copySemantic==CopySemantic.SELF){
            copyAttribute.set(get());
        } else {
            for (T item: get()){
                final T itemCopy = (T) dataCopyProvider.apply(item);
                if (itemCopy!=null){
                    copyAttribute.get().add(itemCopy);
                }
            }
        }
    }

    private CopySemantic copySemantic = CopySemantic.COPY;
    @SuppressWarnings("unchecked")
    public <A extends ReferenceListAttribute<T>> A setCopySemantic(CopySemantic copySemantic){
        this.copySemantic=copySemantic;
        return (A)this;
    }

    public boolean contains(T value) {
        return get().contains(value);
    }

    public void forEach(Consumer<? super T> action) {
        get().forEach(action);
    }

    public T get(int i) {
        return list.get(i);
    }

    @JsonProperty
    ObservableList<T> getList() {
        return list;
    }

    @JsonProperty
    void setList(ObservableList<T> list) {
        this.list = ((ObservableListJacksonAbleWrapper<T>)list).unwrap();
    }

    public void remove(T value) {
        get().remove(value);
    }

    public void set(int i, T value) {
        get().set(i, value);
    }

    public int size() {
        return get().size();
    }

    public Stream<T> stream() {
        return get().stream();
    }

    Map<AttributeChangeListener<ObservableList<T>>, ListChangeListener<T>> listeners= new HashMap<>();
    @Override
    public void addListener(AttributeChangeListener<ObservableList<T>> listener) {
        ListChangeListener<T> listListener = change -> listener.changed(ReferenceListAttribute.this,get());
        listeners.put(listener,listListener);
        list.addListener(listListener);
    }
    @Override
    public void removeListener(AttributeChangeListener<ObservableList<T>> listener) {
        ListChangeListener<T> listListenerToRemove = listeners.get(listener);
        if (listListenerToRemove==null){
            for (Map.Entry<AttributeChangeListener<ObservableList<T>>, ListChangeListener<T>> entry: listeners.entrySet()){
                if (entry.getKey().unwrap()==listener){
                    listListenerToRemove = entry.getValue();
                    listeners.remove(entry.getKey());
                    break;
                }
            }
        }
        list.removeListener(listListenerToRemove);
        listeners.remove(listener);
//        listeners.remove(listener.unwrap());
    }

    @Override
    public String getDisplayText() {
        StringBuilder stringBuilder = new StringBuilder("List (number of entries: "+ list.size()+")\n");
        for (T item:  list){
            stringBuilder.append(item.internal().getDisplayText());
            stringBuilder.append(",\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public void visit(AttributeVisitor attributeVisitor) {
        attributeVisitor.referenceList(this);
    }

    @Override
    @JsonIgnore
    public AttributeTypeInfo getAttributeType() {
        return new AttributeTypeInfo(ObservableList.class,null,null,Data.class, AttributeTypeInfo.AttributeTypeCategory.REFERENCE_LIST, null);
    }


    private Optional<Function<Data,Collection<T>>> possibleValueProviderFromRoot=Optional.empty();
    private Optional<Supplier<T>> newValueProvider =Optional.empty();

    /**customise the list of selectable items*/
    @SuppressWarnings("unchecked")
    public <A extends ReferenceListAttribute<T>> A possibleValueProvider(Function<Data,Collection<T>> provider){
        possibleValueProviderFromRoot=Optional.of(provider);
        return (A)this;
    }

    /**customise how new values are created*/
    @SuppressWarnings("unchecked")
    public <A extends ReferenceListAttribute<T>> A newValueProvider(Supplier<T> newValueProvider){
        this.newValueProvider =Optional.of(newValueProvider);
        return (A)this;
    }

    public T addNewFactory(){
        T addedFactory=null;
        if (newValueProvider.isPresent()) {
            T newFactory = newValueProvider.get().get();
            get().add(newFactory);
            addedFactory = newFactory;
        }

        if (!newValueProvider.isPresent()){
            try {
                T newFactory = clazz.newInstance();
                get().add(newFactory);
                addedFactory = newFactory;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        for (Data data: get()){
            data.internal().prepareUsage(root);
        }

        return addedFactory;
    }

    public void addFactory(T addedFactory){
        get().add(addedFactory);
        for (Data data: get()){
            data.internal().prepareUsage(root);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> possibleValues(){
        ArrayList<T> result = new ArrayList<>();
        possibleValueProviderFromRoot.ifPresent(factoryBaseListFunction -> {
            Collection<T> factories = factoryBaseListFunction.apply(root);
            factories.forEach(factory -> result.add(factory));
        });
        if (!possibleValueProviderFromRoot.isPresent()){
            for (Data factory: root.internal().collectChildrenDeep()){
                if (clazz.isAssignableFrom(factory.getClass())){
                    result.add((T) factory);
                }
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void prepareUsage(Data root, Data parent){
        this.root=root;;
    }

    private boolean userEditable=true;
    /**
     * marks the reference as readonly for the user(user can still navigate but not change the reference)
     */
    @SuppressWarnings("unchecked")
    public <A extends ReferenceListAttribute<T>> A userReadOnly(){
        userEditable=false;
        return (A)this;
    }

    @JsonIgnore
    public boolean isUserEditable(){
        return userEditable;
    }

    private boolean userSelectable=true;
    /**
     * disable select for reference (slect dialog)
     */
    @SuppressWarnings("unchecked")
    public <A extends ReferenceListAttribute<T>> A userNotSelectable(){
        userSelectable=false;
        return (A)this;
    }

    @JsonIgnore
    public boolean isUserSelectable(){
        return userSelectable;
    }

}
