package io.github.factoryfx.factory.attribute.dependency;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import io.github.factoryfx.factory.attribute.*;
import io.github.factoryfx.factory.FactoryBase;

/**
 *
 * @param <R> root
 * @param <L> live object factory
 * @param <F> factory
 */
@JsonIgnoreType
public class FactoryViewListAttribute<R extends FactoryBase<?,R>,L, F extends FactoryBase<L,R>> extends Attribute<List<F>, FactoryViewListAttribute<R,L, F>> implements RunLaterAble, FactoryChildrenEnclosingAttribute<R, FactoryViewListAttribute<R,L, F>> {


    private R root;
    protected final Function<R,List<F>> view;

    public FactoryViewListAttribute(Function<R,List<F>> view) {
        super();
        this.view=view;
    }

    public List<L> instances(){
        ArrayList<L> result = new ArrayList<>();
        for(F item: get()){
            result.add(item.internal().instance());
        }
        return result;
    }

    public boolean add(F data){
        return get().add(data);
    }

    public L instance(Predicate<F> filter){
        Optional<F> any = get().stream().filter(filter).findAny();
        return any.map(t -> t.internal().instance()).orElse(null);
    }

    @Override
    public boolean internal_mergeMatch(List<F> value) {
        final List<F> list = get();
        if (value==null ){
            return false;
        }
        if (list.size() != value.size()) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            if (!referenceEquals(list.get(i), value.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean referenceEquals(FactoryBase<?,?> ref1, FactoryBase<?,?> ref2) {
        if (ref1 == null && ref2 == null) {
            return true;
        }
        if (ref1 == null || ref2 == null) {
            return false;
        }
        return ref1.idEquals(ref2);
    }

    @Override
    public <RL extends FactoryBase<?,RL>> void internal_fixDuplicateObjects(Map<UUID, FactoryBase<?,RL>> idToDataMap) {
        //nothing
    }

    @Override
    public List<F> get() {
        List<F> result = view.apply(root);
        if (result==null){
            return new ArrayList<>();
        }
        return result;
    }

    @Override
    public void set(List<F> value) {
        //nothing
    }

//    @Override
//    public void internal_copyTo(FactoryViewListReferenceAttribute<R,L,T> copyAttribute, FactoryBase.DataCopyProvider dataCopyProvider) {
//        //nothing
//    }

    @Override
    public void internal_semanticCopyTo(FactoryViewListAttribute<R,L, F> copyAttribute) {
        //nothing
    }

    //** so we don't need to initialise javax toolkit for tests*/
    Consumer<Runnable> runlaterExecutor;
    @Override
    public void setRunlaterExecutor(Consumer<Runnable> runlaterExecutor){
        this.runlaterExecutor=runlaterExecutor;
    }

    private void runLater(Runnable runnable){
        if (runlaterExecutor!=null){
            runlaterExecutor.accept(runnable);
        }
    }

    class DirtyTrackingThread extends Thread{
        volatile boolean tracking=true;
        List<F> previousList;
        @Override
        public void run() {
            super.run();
            while(tracking){
                List<F>  currentList = get();
                if ((isEmpty(previousList) && !isEmpty(currentList)) ||
                        (!isEmpty(previousList) && isEmpty(currentList))){

                    for (AttributeChangeListener<List<F>, FactoryViewListAttribute<R,L, F>> listener: new ArrayList<>(listeners)){
                        runLater(()-> listener.changed(FactoryViewListAttribute.this,currentList));
                    }
                }
                if (!isEmpty(previousList) && !isEmpty(currentList)) {
                    HashSet<Object> idList=new HashSet<>();
                    previousList.stream().map(FactoryBase::getId).forEach(idList::add);
                    boolean changed=false;
                    for(F item: currentList){
                        if (!idList.contains(item.getId())){
                            changed=true;
                            break;
                        }
                    }
                    if (changed){
                        for (AttributeChangeListener<List<F>, FactoryViewListAttribute<R,L, F>> listener: new ArrayList<>(listeners)){
                            runLater(()->listener.changed(FactoryViewListAttribute.this,currentList));
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

        private boolean isEmpty(List<F> list){
            return list == null || list.isEmpty();
        }

        public void stopTracking() {
            tracking=false;
        }
    }
    DirtyTrackingThread dirtyTracking;

    List<AttributeChangeListener<List<F>, FactoryViewListAttribute<R,L, F>>> listeners;
    @Override
    public void internal_addListener(AttributeChangeListener<List<F>, FactoryViewListAttribute<R,L, F>> listener) {
        if (listeners==null){
            listeners=Collections.synchronizedList(new ArrayList<>());
        }
        listeners.add(listener);
        if (dirtyTracking==null){
            dirtyTracking = new DirtyTrackingThread();
            dirtyTracking.setDaemon(true);
            dirtyTracking.start();
        }
    }
    @Override
    public void internal_removeListener(AttributeChangeListener<List<F>, FactoryViewListAttribute<R,L, F>> listener) {
        if (listeners!=null) {
            for (AttributeChangeListener<List<F>, FactoryViewListAttribute<R,L, F>> listenerItem: new ArrayList<>(listeners)){
                if (listenerItem.unwrap()==listener){
                    listeners.remove(listenerItem);
                }
            }
            if (listeners.isEmpty() && dirtyTracking != null){
                dirtyTracking.stopTracking();
                dirtyTracking=null;
            }

        }
    }

    @Override
    public String getDisplayText() {
        return new CollectionAttributeUtil<>(get(), t -> t.internal().getDisplayText()).getDisplayText();
    }

    @Override
    public void internal_addBackReferences(R root, FactoryBase<?,R> parent){
        this.root=root;
    }

    @Override
    public void internal_setReferenceClass(Class<?> clazz) {
        //nothing
    }

    @Override
    public void internal_copyTo(FactoryViewListAttribute<R, L, F> copyAttribute, int level, int maxLevel, List<FactoryBase<?, R>> oldData, FactoryBase<?, R> parent, R root) {
        //nothing
    }

    @Override
    public void internal_endUsage() {
        if (dirtyTracking!=null) {
            dirtyTracking.stopTracking();
        }
        if (listeners!=null){
            listeners.clear();
        }
    }

    public Stream<F> stream() {
        return get().stream();
    }

    @Override
    public boolean internal_ignoreForMerging() {
        return true;
    }

    @Override
    public void internal_visitChildren(Consumer<FactoryBase<?, R>> consumer, boolean includeViews) {
        if (includeViews){
            List<F> children = get();
            for (F factory : children) {
                consumer.accept(factory);
            }
        }
    }
}
