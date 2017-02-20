package de.factoryfx.factory;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.factoryfx.data.Data;
import de.factoryfx.data.attribute.ViewListReferenceAttribute;
import de.factoryfx.data.attribute.ViewReferenceAttribute;
import de.factoryfx.factory.atrribute.FactoryReferenceAttribute;
import de.factoryfx.factory.atrribute.FactoryReferenceListAttribute;
import de.factoryfx.factory.atrribute.FactoryViewListReferenceAttribute;
import de.factoryfx.factory.atrribute.FactoryViewReferenceAttribute;

/**
 * @param <L> liveobject created from this factory
 * @param <V> runtime visitor
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class FactoryBase<L,V> extends Data {

    private String id;

    public FactoryBase() {

    }

    @Override
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String value) {
        id = value;
    }
    public void setId(Object value) {
        id = (String)value;
    }


    @JsonIgnore
    private L createdLiveObject;
    @JsonIgnore
    private boolean started=false;
    @JsonIgnore
    boolean needRecreation =false;

    private L previousLiveObject;

    private L instance() {
        if (needRecreation){
            previousLiveObject = this.createdLiveObject;
            this.createdLiveObject = reCreate(previousLiveObject);
            needRecreation=false;
            started=false;
        } else {
            if (createdLiveObject==null){
                createdLiveObject = create();
            }
        }
        return createdLiveObject;
    }

    L create(){
        if (creator!=null){
            return creator.get();
        }
        throw new IllegalStateException("no creator defined: "+getClass());
    }

    private L reCreate(L previousLiveObject) {
        if (reCreatorWithPreviousLiveObject!=null){
            return reCreatorWithPreviousLiveObject.apply(previousLiveObject);
        }
        return create();
    }

    private void start() {
        if (!started && starterWithNewLiveObject!=null && createdLiveObject!=null){//createdLiveObject is null e.g. if object ist not instanced in the parent factory
            starterWithNewLiveObject.accept(createdLiveObject);
            started=true;
        }
    }

    private void destroy(Set<FactoryBase<?,V>> previousFactories) {
        if (!previousFactories.contains(this) && destroyerWithPreviousLiveObject!=null){
            destroyerWithPreviousLiveObject.accept(createdLiveObject);
        }
        if (previousLiveObject!=null && destroyerWithPreviousLiveObject!=null){
            destroyerWithPreviousLiveObject.accept(previousLiveObject);
        }
        previousLiveObject=null;
    }

    private void determineRecreationNeed(Set<FactoryBase<?,?>> changedFactories, ArrayDeque<FactoryBase<?,?>> path){
        path.push(this);

        needRecreation =changedFactories.contains(this) || createdLiveObject==null;  //null means newly added
        if (needRecreation){
            for (FactoryBase factoryBase: path){
                factoryBase.needRecreation =true;
            }
        }

        visitChildFactoriesAndViewsFlat(vFactoryBase -> vFactoryBase.determineRecreationNeed(changedFactories,path));
        path.pop();
    }

    private LoopProtector loopProtector = new LoopProtector();
    private void loopDetector(){
        loopProtector.enter();
        try {
            this.internalFactory().visitChildFactoriesAndViewsFlat(factory -> cast(factory).ifPresent(FactoryBase::loopDetector));
        } finally {
            loopProtector.exit();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<FactoryBase<?,V>> cast(Data data){
        if (data instanceof FactoryBase)
            return Optional.of((FactoryBase<?,V>)data);
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Set<FactoryBase<?,V>> collectChildFactoriesDeep(){
        return super.internal().collectChildrenDeep().stream().map(this::cast).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    private Set<FactoryBase<?,V>> collectChildrenFactoriesFlat() {
        return super.internal().collectChildrenFlat().stream().map(this::cast).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }




    private String debugInfo(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ID:\n  ");
        stringBuilder.append(getId());
        stringBuilder.append("\nAttributes:\n");
        this.internal().visitAttributesFlat((attributeVariableName, attribute) -> {
            stringBuilder.append("  ").append(attributeVariableName).append(": ").append(attribute.getDisplayText()).append("\n");
        });
        return stringBuilder.toString().trim();
    }


    private void runtimeQuery(V visitor) {
        if (executorWidthVisitorAndCurrentLiveObject!=null){
            executorWidthVisitorAndCurrentLiveObject.accept(visitor,createdLiveObject);
        }
    }

    private void visitChildFactoriesAndViewsFlat(Consumer<FactoryBase<?,V>> consumer) {
        this.internal().visitAttributesFlat((attributeVariableName, attribute) -> {
            if (attribute instanceof FactoryReferenceAttribute) {
                ((FactoryReferenceAttribute<?,?>)attribute).getOptional().ifPresent(data -> cast(data).ifPresent(consumer::accept));
            }
            if (attribute instanceof FactoryReferenceListAttribute) {
                ((FactoryReferenceListAttribute<?,?>)attribute).forEach(data -> cast(data).ifPresent(consumer::accept));
            }
            if (attribute instanceof FactoryViewReferenceAttribute) {
                ((ViewReferenceAttribute<?,?>)attribute).getOptional().ifPresent(data -> cast(data).ifPresent(consumer::accept));
            }
            if (attribute instanceof FactoryViewListReferenceAttribute) {
                ((ViewListReferenceAttribute<?,?>)attribute).get().forEach(data -> cast(data).ifPresent(consumer::accept));
            }
        });
    }

    FactoryInternal<L,V> factoryInternal = new FactoryInternal<>(this);
    /** <b>internal methods should be only used from the framework.</b>
     *  They may change in the Future.
     *  There is no fitting visibility in java therefore this workaround.
     */
    public FactoryInternal<L,V> internalFactory(){
        return factoryInternal;
    }

    public static class FactoryInternal<L,V> {
        private final FactoryBase<L,V> factory;

        public FactoryInternal(FactoryBase<L, V> factory) {
            this.factory = factory;
        }

        /** create and prepare the liveobject*/
        public L create(){
            return factory.create();
        }

        /**determine which live objects needs recreation*/
        public void determineRecreationNeed(Set<FactoryBase<?,?>> changedFactories) {
            factory.determineRecreationNeed(changedFactories,new ArrayDeque<>());
        }

        /** start the liveOobject e.g open a port*/
        public void start() {
            factory.start();
        }

        /** start the liveObject e.g open a port*/
        public void destroy(Set<FactoryBase<?,V>> previousFactories) {
            factory.destroy(previousFactories);
        }

        /** execute visitor to get runtime informations from the liveobject*/
        public void runtimeQuery(V visitor) {
            factory.runtimeQuery(visitor);
        }

        public void visitChildFactoriesAndViewsFlat(Consumer<FactoryBase<?,V>> consumer) {
            factory.visitChildFactoriesAndViewsFlat(consumer);
        }

        public L instance() {
           return factory.instance();
        }

        public  void loopDetector() {
            factory.loopDetector();
        }

        public Set<FactoryBase<?,V>> collectChildFactoriesDeep(){
            return factory.collectChildFactoriesDeep();
        }

        public Set<FactoryBase<?,V>> collectChildrenFactoriesFlat() {
            return factory.collectChildrenFactoriesFlat();
        }

        public String debugInfo() {
            return factory.debugInfo();
        }


    }

    Supplier<L> creator=null;
    Function<L,L> reCreatorWithPreviousLiveObject=null;
    Consumer<L> starterWithNewLiveObject=null;
    Consumer<L> destroyerWithPreviousLiveObject=null;
    BiConsumer<V,L> executorWidthVisitorAndCurrentLiveObject=null;
    private void setCreator(Supplier<L> creator){
        this.creator=creator;
    }

    private void setReCreator(Function<L,L> reCreatorWithPreviousLiveObject ) {
        this.reCreatorWithPreviousLiveObject=reCreatorWithPreviousLiveObject;
    }

    private void setStarter(Consumer<L> starterWithNewLiveObject) {
        this.starterWithNewLiveObject=starterWithNewLiveObject;
    }

    private void setDestroyer(Consumer<L> destroyerWithPreviousLiveObject) {
        this.destroyerWithPreviousLiveObject=destroyerWithPreviousLiveObject;
    }

    private void setRuntimeQueryExecutor(BiConsumer<V,L> executorWidthVisitorAndCurrentLiveObject) {
        this.executorWidthVisitorAndCurrentLiveObject=executorWidthVisitorAndCurrentLiveObject;
    }

    LiveCycleConfig<L,V> liveCycleConfig = new LiveCycleConfig<>(this);

    /** live cycle configurations api
     *
     * Update Order
     * 1. recreate for changed, create for new
     * 2. destroy removed, updated
     * 3. start new
     *
     * The goal is to keep the time between destroy and start as short as possible cause that's essentially the application downtime.
     * Therefore slow operation should be executed in create.
     * */
    public LiveCycleConfig<L,V> configLiveCycle(){
        return liveCycleConfig;
    }

    public static class LiveCycleConfig<L,V> {
        private final FactoryBase<L,V> factory;

        public LiveCycleConfig(FactoryBase<L, V> factory) {
            this.factory = factory;
        }

        /**create and prepare the liveObject*/
        public void setCreator(Supplier<L> creator){
            factory.setCreator(creator);
        }

        /**the factory data has changed therefore a new liveobject is needed.<br>
         * previousLiveObject can be used to reuse resources like connection pools etc.<br>
         * passed old liveobject is never null
         * */
        public void setReCreator(Function<L,L> reCreatorWithPreviousLiveObject ) {
            factory.setReCreator(reCreatorWithPreviousLiveObject);
        }

        /** start the liveObject e.g open a port*/
        public void setStarter(Consumer<L> starterWithNewLiveObject) {
            factory.setStarter(starterWithNewLiveObject);
        }

        /** finally free liveObject e.g close a port*/
        public void setDestroyer(Consumer<L> destroyerWithPreviousLiveObject) {
            factory.setDestroyer(destroyerWithPreviousLiveObject);
        }

        /**execute visitor to get runtime information from the liveobjects*/
        public void setRuntimeQueryExecutor(BiConsumer<V,L> executorWidthVisitorAndCurrentLiveObject) {
            factory.setRuntimeQueryExecutor(executorWidthVisitorAndCurrentLiveObject);
        }
    }
}
