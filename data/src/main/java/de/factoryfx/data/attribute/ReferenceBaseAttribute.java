package de.factoryfx.data.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.factoryfx.data.Data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ReferenceBaseAttribute<T extends Data, U, A extends ReferenceBaseAttribute<T,U,A>> extends Attribute<U> {

    protected Data root;
    protected Class<T> containingFactoryClass;


    /**
     *
     * @param containingFactoryClass  class off the factory in this Attribute, workaround for java generics Type Erasure
     * @param attributeMetadata AttributeMetadata
     */
    public ReferenceBaseAttribute(Class<T> containingFactoryClass, AttributeMetadata attributeMetadata) {
        super(attributeMetadata);
        this.containingFactoryClass = containingFactoryClass;
    }

    /**
     * @param attributeMetadata AttributeMetadata
     * @param containingFactoryClass class off the factory in this Attribute, workaround for java generics Type Erasure.<br>( workaround for generic parameter ReferenceAttribute<Example<V>> webGuiResource=new ReferenceAttribute(Example<V>) )
     */
    @SuppressWarnings("unchecked")
    public ReferenceBaseAttribute(AttributeMetadata attributeMetadata, Class containingFactoryClass) {
        super(attributeMetadata);
        this.containingFactoryClass = containingFactoryClass;
    }

    private Function<Data,Collection<T>> possibleValueProviderFromRoot;

    /**customise the list of selectable items*/
    @SuppressWarnings("unchecked")
    public A possibleValueProvider(Function<Data,Collection<T>> provider){
        possibleValueProviderFromRoot=provider;
        return (A)this;
    }

    @SuppressWarnings("unchecked")
    public Collection<T> internal_possibleValues(){
        if (possibleValueProviderFromRoot==null){
            Set<T> result = new HashSet<>();
            for (Data factory: root.internal().collectChildrenDeep()){
                if (containingFactoryClass.isAssignableFrom(factory.getClass())){
                    result.add((T) factory);
                }
            }
            return result;
        } else {
            return possibleValueProviderFromRoot.apply(root);
        }
    }

    @Override
    public void internal_prepareUsage(Data root){
        this.root=root;
    }

    protected Function<Data,T> newValueProvider;
    /**
     * customise how new values are created
     * @param newValueProviderFromRoot value, root
     * @return the new added factory
     */
    @SuppressWarnings("unchecked")
    public A newValueProvider(Function<Data,T> newValueProviderFromRoot){
        this.newValueProvider =newValueProviderFromRoot;
        return (A)this;
    }

    protected BiConsumer<T,Data> additionalDeleteAction;
    /**
     * action after delete, e.g delete the factory also in other lists
     * @param additionalDeleteAction deleted value, root
     * @return this
     */
    @SuppressWarnings("unchecked")
    public A additionalDeleteAction(BiConsumer<T,Data> additionalDeleteAction){
        this.additionalDeleteAction=additionalDeleteAction;
        return (A)this;
    }

    private boolean userEditable=true;
    /**
     * marks the reference as readonly for the user(user can still navigate but not change the reference)
     */
    @SuppressWarnings("unchecked")
    public A userReadOnly(){
        userEditable=false;
        return (A)this;
    }

    @JsonIgnore
    public boolean internal_isUserEditable(){
        return userEditable;
    }

    private boolean userSelectable=true;
    /**
     * disable select for reference, used in gui to disable the select button so that the user can't select new factories in this attribute
     */
    @SuppressWarnings("unchecked")
    public A userNotSelectable(){
        userSelectable=false;
        return (A)this;
    }

    @JsonIgnore
    public boolean internal_isUserSelectable(){
        return userSelectable;
    }


    private boolean userCreatable =true;
    /**
     * disable new for reference, used in gui to disable the new button so that the user can't create new factories in this attribute
     */
    @SuppressWarnings("unchecked")
    public A userNotCreatable(){
        userCreatable =false;
        return (A)this;
    }

    @JsonIgnore
    public boolean internal_isUserCreatable(){
        return userCreatable;
    }
}