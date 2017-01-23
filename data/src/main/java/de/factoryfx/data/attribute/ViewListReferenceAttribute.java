package de.factoryfx.data.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import de.factoryfx.data.Data;
import de.factoryfx.data.merge.attribute.AttributeMergeHelper;
import de.factoryfx.data.merge.attribute.NopMergeHelper;
import javafx.application.Platform;
import javafx.collections.ObservableList;

@JsonIgnoreType
public class ViewListReferenceAttribute <R extends Data, T extends Data> extends Attribute<List<T>> {
    private R root;
    private final Function<R,List<T>> view;

    public ViewListReferenceAttribute(AttributeMetadata attributeMetadata, Function<R,List<T>> view) {
        super(attributeMetadata);
        this.view=view;
    }

    @Override
    public void internal_collectChildren(Set<Data> allModelEntities) {
        //nothing
    }

    @Override
    public AttributeMergeHelper<?> internal_createMergeHelper() {
        return new NopMergeHelper();
    }

    @Override
    public void internal_fixDuplicateObjects(Function<Object, Optional<Data>> getCurrentEntity) {
        //nothing
    }

    @Override
    public List<T> get() {
        return view.apply(root);
    }

    @Override
    public void set(List<T> value) {
        //nothing
    }

    @Override
    public void internal_copyTo(Attribute<List<T>> copyAttribute, Function<Data,Data> dataCopyProvider) {
        //nothing
    }

    @Override
    public void internal_semanticCopyTo(Attribute<List<T>> copyAttribute, Function<Data,Data> dataCopyProvider) {
        //nothing
    }

    //** so we don't need to initialise javax toolkit*/
    Consumer<Runnable> runlaterExecutor=(r)-> Platform.runLater(r);
    void setRunlaterExecutorForTest(Consumer<Runnable> runlaterExecutor){
        this.runlaterExecutor=runlaterExecutor;
    }

    public void runLater(Runnable runnable){
        runlaterExecutor.accept(runnable);
    }

    class DirtyTrackingThread extends Thread{
        volatile boolean tracking=true;
        List<T> previousList;
        @Override
        public void run() {
            super.run();
            while(tracking){
                List<T>  currentList = get();
                if ((isEmpty(previousList) && !isEmpty(currentList)) ||
                    (!isEmpty(previousList) && isEmpty(currentList))){

                    for (AttributeChangeListener<List<T>> listener: new ArrayList<>(listeners)){
                        runLater(()-> listener.changed(ViewListReferenceAttribute.this,currentList));
                    }
                }
                if (!isEmpty(previousList) && !isEmpty(currentList)) {
                    HashSet<Object> idList=new HashSet<>();
                    previousList.stream().map(Data::getId).forEach(idList::add);
                    boolean changed=false;
                    for(T item: currentList){
                        if (!idList.contains(item.getId())){
                            changed=true;
                            break;
                        }
                    }
                    if (changed){
                        for (AttributeChangeListener<List<T>> listener: new ArrayList<>(listeners)){
                            listener.changed(ViewListReferenceAttribute.this,currentList);
                        }
                    }
                }
                previousList= currentList;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private boolean isEmpty(List<T> list){
            return list == null || list.isEmpty();
        }

        public void stopTracking() {
            tracking=false;
        }
    }
    DirtyTrackingThread dirtyTracking;

    List<AttributeChangeListener<List<T>>> listeners= Collections.synchronizedList(new ArrayList<>());
    @Override
    public void internal_addListener(AttributeChangeListener<List<T>> listener) {
        listeners.add(listener);
        if (dirtyTracking==null){
            dirtyTracking = new DirtyTrackingThread();
            dirtyTracking.setDaemon(true);
            dirtyTracking.start();
        }
    }
    @Override
    public void internal_removeListener(AttributeChangeListener<List<T>> listener) {
        for (AttributeChangeListener<List<T>> listenerItem: new ArrayList<>(listeners)){
            if (listenerItem.unwrap()==listener){
                listeners.remove(listenerItem);
            }
        }
        if (listeners.isEmpty() && dirtyTracking != null){
            dirtyTracking.stopTracking();
            dirtyTracking=null;
        }
    }


    @Override
    public String getDisplayText() {
        List<T> list = get();
        StringBuilder stringBuilder = new StringBuilder("List (number of entries: "+ list.size()+")\n");
        for (T item: list){
            stringBuilder.append(item.internal().getDisplayText());
            stringBuilder.append(",\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public void internal_visit(AttributeVisitor attributeVisitor) {
        //nothing
    }

    @JsonIgnore
    @Override
    public AttributeTypeInfo internal_getAttributeType() {
        return new AttributeTypeInfo(ObservableList.class,null,null,Data.class, AttributeTypeInfo.AttributeTypeCategory.REFERENCE_LIST, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void internal_prepareUsage(Data root){
        this.root=(R)root;;
    }

    @Override
    public void internal_endUsage() {
        if (dirtyTracking!=null) {
            dirtyTracking.stopTracking();
        }
        listeners.clear();
    }
}
