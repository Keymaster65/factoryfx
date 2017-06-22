package de.factoryfx.data.attribute;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.factoryfx.data.Data;
import de.factoryfx.data.validation.ObjectRequired;
import de.factoryfx.data.validation.Validation;
import de.factoryfx.data.validation.ValidationError;

public abstract class Attribute<T,A extends Attribute<T,A>>{

    @JsonIgnore
    private final List<Validation<T>> validations = new ArrayList<>();

    public Attribute() {

    }

    public abstract void internal_collectChildren(Set<Data> allModelEntities);

    public abstract Attribute<T,A> internal_copy();

    public abstract boolean internal_match(T value);

    public boolean ignoreForMerging() {
        return false;
    }

    /**
     * merge logic delegate AttributeMergeHelper
     * @return MergeHelper or null if no merging should be executed
     */
    public boolean hasMergeConflict(Attribute<?,?> originalAttribute, Attribute<?,?> newAttribute) {
        if (newAttribute.internal_match(originalAttribute)) {
            return false;
        }
        if (internal_match(originalAttribute)) {
            return false;
        }
        if (internal_match(newAttribute)) {
            return false;
        }
        return true;
    }

    /**
     * check if merge should be executed e.g. not if values ar equals
     * */
    public boolean isMergeable(Attribute<?,?> originalAttribute, Attribute<?,?> newAttribute) {
        if (!internal_match(originalAttribute) || internal_match(newAttribute)) {
            return false ;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public void merge(Attribute<?,?> newValue) {
        set((T) newValue.get());
    }

    /*
        see test {{@Link MergeTest#test_duplicate_ids_bug}} why this is needed
    */
    public abstract void internal_fixDuplicateObjects(Map<String, Data> idToDataMap);

    public abstract void internal_copyTo(A copyAttribute, Function<Data,Data> dataCopyProvider);

    @SuppressWarnings("unchecked")
    public void internal_copyToUnsafe(Attribute<?,?> copyAttribute, Function<Data,Data> dataCopyProvider){
        internal_copyTo((A)copyAttribute,dataCopyProvider);
    }

    @SuppressWarnings("unchecked")
    public boolean internal_match(Attribute<?,?> attribute) {
        return internal_match((T) attribute.get());
    }

    public abstract void internal_semanticCopyTo(A copyAttribute);

    @SuppressWarnings("unchecked")
    public void internal_semanticCopyToUnsafe(Attribute<?,?> copyAttribute){
        internal_semanticCopyTo((A)copyAttribute);
    };

    public List<ValidationError> internal_validate(Data parent) {
        List<ValidationError> validationErrors = new ArrayList<>();
        for (Validation<T> validation : validations) {
            if (!validation.validate(get())){
                validationErrors.add(new ValidationError(validation.getValidationDescription(),this,parent));
            }
        }
        return validationErrors;
    }

    public boolean internal_required() {
        for (Validation<?> validation: validations){
            if (validation instanceof ObjectRequired<?>) {
                return true;
            }
        }
        return false;
    }


    public abstract void internal_visit(AttributeVisitor attributeVisitor);

    public void internal_visit(Consumer<Data> childFactoriesVisitor){
        internal_visit(new AttributeVisitor() {
            @Override
            public void value(Attribute<?,?> value) {

            }

            @Override
            public void reference(ReferenceAttribute<?,?> reference) {
                reference.getOptional().ifPresent(childFactoriesVisitor::accept);
            }

            @Override
            public void referenceList(ReferenceListAttribute<?,?> referenceList) {
                referenceList.get().forEach(childFactoriesVisitor::accept);
            }
        });
    }

    public abstract AttributeTypeInfo internal_getAttributeType();

    public void internal_prepareUsage(Data root){
        //nothing
    }

    /**all elements prepared and root is usable*/
    public void internal_afterPreparedUsage(Data root){
        //nothing
    }

    public void internal_endUsage() {
        //nothing
    }

    public abstract void internal_addListener(AttributeChangeListener<T,A> listener);

    /** remove added Listener or Listener inside WeakAttributeChangeListener*/
    public abstract void internal_removeListener(AttributeChangeListener<T,A> listener);

    @SuppressWarnings("unchecked")
    public A defaultValue(T defaultValue) {
        set(defaultValue);
        return (A)this;
    }

    public abstract T get();

    public abstract void set(T value);

    @JsonIgnore
    public abstract String getDisplayText();

    @SuppressWarnings("unchecked")
    public A validation(Validation<T> validation){
        this.validations.add(validation);
        return (A)this;
    }
    private static final Locale PORTUGUESE =new Locale("pt", "PT");
    private static final Locale SPANISH=new Locale("es", "ES");
    public String internal_getPreferredLabelText(Locale locale){
        if (en!=null && locale.equals(Locale.ENGLISH)){
            return en;
        }
        if (de!=null && locale.equals(Locale.GERMAN)){
            return de;
        }
        if (es!=null && locale.equals(SPANISH)){
            return es;
        }
        if (fr!=null && locale.equals(Locale.FRANCE)){
            return fr;
        }
        if (it!=null && locale.equals(Locale.ITALIAN)){
            return it;
        }
        if (pt!=null && locale.equals(PORTUGUESE)){
            return pt;
        }

        if (en!=null){
            return en;
        }
        if (de!=null){
            return de;
        }
        if (es!=null){
            return es;
        }
        if (fr!=null){
            return fr;
        }
        if (it!=null){
            return it;
        }
        if (pt!=null){
            return pt;
        }

        return "";
    }

    public String internal_getAddonText(){
        return addonText;
    }

    public boolean internal_hasWritePermission(Function<String,Boolean> permissionChecker){
        return permission == null || permissionChecker.apply(permission);
    }

    @JsonIgnore
    private String permission;
    public A permission(String permission){
        this.permission = permission;
        return (A)this;
    }

    @JsonIgnore
    private String addonText;

    /**
     * add-on text for the attribute, text that is displayed an the right side of the input usually used for units,%,currency symbol etc
     */
    public A addonText(String addonText){
        this.addonText=addonText;
        return (A)this;
    }


    @SuppressWarnings("unchecked")
    public A labelText(String text){
        en=text;
        return (A)this;
    }

    public A labelText(String labelText, Locale locale){
//        this.labelText.internal_put(locale,labelText);
        return (A)this;
    }

    //stored i single fields to avoids object bloat
    @JsonIgnore
    String en;
    @JsonIgnore
    String de;
    @JsonIgnore
    String es;
    @JsonIgnore
    String fr;
    @JsonIgnore
    String it;
    @JsonIgnore
    String pt;
    public A en(String text) {
        en=text;
        return (A)this;
    }

    public A de(String text) {
        de=text;
        return (A)this;
    }

    public A es(String text) {
        es=text;
        return (A)this;
    }

    public A fr(String text) {
        fr=text;
        return (A)this;
    }

    public A it(String text) {
        it=text;
        return (A)this;
    }

    public A pt(String text) {
        pt=text;
        return (A)this;
    }

    public void takeContentFromAttribute(A attribute){
        this.en=attribute.en;
        this.de=attribute.de;
        this.es=attribute.es;
        this.fr=attribute.fr;
        this.it=attribute.it;
        this.pt=attribute.pt;
        set(get());
    }




}
