package de.factoryfx.javafx.data.editor.attribute;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.factoryfx.javafx.data.attribute.ColorAttribute;
import de.factoryfx.javafx.data.editor.attribute.builder.DataSingleAttributeEditorBuilder;
import de.factoryfx.javafx.data.editor.attribute.builder.NoListSingleAttributeEditorBuilder;
import de.factoryfx.javafx.data.editor.attribute.builder.SimpleSingleAttributeEditorBuilder;
import de.factoryfx.javafx.data.editor.attribute.builder.SingleAttributeEditorBuilder;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;

import org.controlsfx.glyphfont.FontAwesome;

import com.google.common.base.Ascii;

import de.factoryfx.data.Data;
import de.factoryfx.data.attribute.Attribute;
import de.factoryfx.data.attribute.ReferenceAttribute;
import de.factoryfx.data.attribute.ReferenceListAttribute;
import de.factoryfx.data.attribute.ValueListAttribute;
import de.factoryfx.data.attribute.ViewListReferenceAttribute;
import de.factoryfx.data.attribute.ViewReferenceAttribute;
import de.factoryfx.data.attribute.primitive.BooleanAttribute;
import de.factoryfx.data.attribute.primitive.DoubleAttribute;
import de.factoryfx.data.attribute.primitive.IntegerAttribute;
import de.factoryfx.data.attribute.primitive.LongAttribute;
import de.factoryfx.data.attribute.time.DurationAttribute;
import de.factoryfx.data.attribute.time.LocalDateAttribute;
import de.factoryfx.data.attribute.time.LocalDateTimeAttribute;
import de.factoryfx.data.attribute.time.LocalTimeAttribute;
import de.factoryfx.data.attribute.types.BigDecimalAttribute;
import de.factoryfx.data.attribute.types.EncryptedString;
import de.factoryfx.data.attribute.types.EncryptedStringAttribute;
import de.factoryfx.data.attribute.types.EnumAttribute;
import de.factoryfx.data.attribute.types.I18nAttribute;
import de.factoryfx.data.attribute.types.LocaleAttribute;
import de.factoryfx.data.attribute.types.PasswordAttribute;
import de.factoryfx.data.attribute.types.StringAttribute;
import de.factoryfx.data.attribute.types.URIAttribute;
import de.factoryfx.data.util.LanguageText;
import de.factoryfx.javafx.data.editor.attribute.visualisation.BigDecimalAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.BooleanAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.CatalogListAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.ColorAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.DefaultValueAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.DoubleAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.DurationAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.EncryptedStringAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.EnumAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.ExpandableAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.I18nAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.IntegerAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.LocalDateAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.LocalDateTimeAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.LocalTimeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.LocaleAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.LongAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.PasswordAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.ReferenceAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.ReferenceListAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.StringAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.StringHtmlAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.StringLongAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.URIAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.ViewListReferenceAttributeVisualisation;
import de.factoryfx.javafx.data.editor.attribute.visualisation.ViewReferenceAttributeVisualisation;
import de.factoryfx.javafx.data.util.UniformDesign;
import de.factoryfx.javafx.data.widget.datalistedit.ReferenceListAttributeEditWidget;

public class AttributeEditorBuilder {

    private final List<SingleAttributeEditorBuilder<?>> singleAttributeEditorBuilders;

    public AttributeEditorBuilder(List<SingleAttributeEditorBuilder<?>> singleAttributeEditorBuilders) {
        this.singleAttributeEditorBuilders = singleAttributeEditorBuilders;
    }

    @SuppressWarnings("unchecked")
    public static List<SingleAttributeEditorBuilder<?>> createDefaultSingleAttributeEditorBuilders(UniformDesign uniformDesign){
        ArrayList<SingleAttributeEditorBuilder<?>> result = new ArrayList<>();

        result.add(new SingleAttributeEditorBuilder<EncryptedString>(){
            @Override
            public boolean isEditorFor(Attribute<?, ?> attribute) {
                return attribute instanceof PasswordAttribute;
            }

            @Override
            public AttributeEditor<EncryptedString, ?> createEditor(Attribute<?, ?> attribute, Consumer<Data> navigateToData, Data previousData) {
                PasswordAttribute passwordAttributeVisualisation = (PasswordAttribute) attribute;
                return new AttributeEditor<>(passwordAttributeVisualisation,new PasswordAttributeVisualisation(passwordAttributeVisualisation::internal_hash, passwordAttributeVisualisation::internal_isValidKey,uniformDesign),uniformDesign);

            }
        });

        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,BigDecimalAttribute.class,BigDecimal.class,(attribute)-> new BigDecimalAttributeVisualisation(attribute.internal_getDecimalFormatPattern()),()->new BigDecimalAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,BooleanAttribute.class,Boolean.class,(attribute)-> new BooleanAttributeVisualisation(),()->new BooleanAttribute()));
//        result.add(new SimpleSingleAttributeEditorBuilder<>(ByteArrayAttribute.class,byte[].class,(attribute)->{
//            return new AttributeEditor<BigDecimal>(attribute,new BigDecimalAttributeVisualisation(attribute.internal_getDecimalFormatPattern()),uniformDesign);
//        }));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,ColorAttribute.class,Color.class,(attribute)-> new ColorAttributeVisualisation(),()->new ColorAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,DoubleAttribute.class,Double.class,(attribute)-> new DoubleAttributeVisualisation(),()->new DoubleAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,EncryptedStringAttribute.class,EncryptedString.class,(attribute)-> new EncryptedStringAttributeVisualisation(attribute::createKey, attribute::internal_isValidKey,uniformDesign),()->new EncryptedStringAttribute()));

        result.add(new NoListSingleAttributeEditorBuilder<EnumAttribute.EnumWrapper,EnumAttribute<?>>(uniformDesign,(attribute)->attribute instanceof EnumAttribute,(attribute)->{
            return new EnumAttributeVisualisation(uniformDesign,attribute.internal_possibleEnumValues().stream().map(e->new EnumAttribute.EnumWrapper(e)).collect(Collectors.toList()));
        }));

        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,I18nAttribute.class,LanguageText.class,(attribute)-> new I18nAttributeVisualisation(),()->new I18nAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,IntegerAttribute.class,Integer.class,(attribute)-> new IntegerAttributeVisualisation(),()->new IntegerAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,LocalDateAttribute.class,LocalDate.class,(attribute)-> new LocalDateAttributeVisualisation(),()->new LocalDateAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,LocalDateTimeAttribute.class,LocalDateTime.class,(attribute)-> new LocalDateTimeAttributeVisualisation(),()->new LocalDateTimeAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,LocalTimeAttribute.class,LocalTime.class,(attribute)-> new LocalTimeVisualisation(),()->new LocalTimeAttribute()));

        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,LocaleAttribute.class,Locale.class,(attribute)-> new LocaleAttributeVisualisation(),()->new LocaleAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,LongAttribute.class,Long.class,(attribute)-> new LongAttributeVisualisation(),()->new LongAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign, DurationAttribute.class, Duration.class, (attribute)-> new DurationAttributeVisualisation(), ()->new DurationAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,StringAttribute.class,String.class,(attribute)->{
            if (attribute.internal_isLongText()){
                return new ExpandableAttributeVisualisation<>(new StringLongAttributeVisualisation(),uniformDesign, (s)->Ascii.truncate(s,20,"..."),FontAwesome.Glyph.FONT,attribute.internal_isDefaultExpanded() );
            }
            if (attribute.internal_isHtmlText()){
                return new ExpandableAttributeVisualisation<>(new StringHtmlAttributeVisualisation(),uniformDesign, (s)->Ascii.truncate(s,20,"..."),FontAwesome.Glyph.FONT,attribute.internal_isDefaultExpanded() );
            }
            return new StringAttributeVisualisation();
        },()->new StringAttribute()));
        result.add(new SimpleSingleAttributeEditorBuilder<>(uniformDesign,URIAttribute.class,URI.class,(attribute)-> new URIAttributeVisualisation(),()->new URIAttribute()));
        result.add(new DataSingleAttributeEditorBuilder(uniformDesign,(a)->a instanceof ViewReferenceAttribute,(attribute, navigateToData, previousData)-> new ViewReferenceAttributeVisualisation(navigateToData, uniformDesign)));
        result.add(new DataSingleAttributeEditorBuilder(uniformDesign,(a)->a instanceof ViewListReferenceAttribute,(attribute, navigateToData, previousData)->{
            ViewListReferenceAttributeVisualisation visualisation = new ViewListReferenceAttributeVisualisation(navigateToData, uniformDesign);
            ExpandableAttributeVisualisation<List<Data>> expandableAttributeVisualisation= new ExpandableAttributeVisualisation<>(visualisation,uniformDesign,(l)->"Items: "+l.size(),FontAwesome.Glyph.LIST);
            if (((ViewListReferenceAttribute)attribute).get().contains(previousData)){
                expandableAttributeVisualisation.expand();
            }
            return expandableAttributeVisualisation;
        }));

        result.add(new SingleAttributeEditorBuilder<Data>(){
            @Override
            public boolean isEditorFor(Attribute<?, ?> attribute) {
                return attribute instanceof ReferenceAttribute;
            }

            @Override
            public AttributeEditor<Data, ?> createEditor(Attribute<?, ?> attribute, Consumer<Data> navigateToData, Data previousData) {
                ReferenceAttribute referenceAttribute = (ReferenceAttribute) attribute;
                return new AttributeEditor<>(referenceAttribute,
                                             new ReferenceAttributeVisualisation(uniformDesign,
                                                                                 navigateToData,
                                                                                 referenceAttribute::internal_createNewPossibleValues,
                                                                                 referenceAttribute::set,
                                                                                 referenceAttribute::internal_possibleValues,
                                                                                 referenceAttribute::internal_deleteFactory,
                                                                                 referenceAttribute.internal_isUserEditable(),
                                                                                 referenceAttribute.internal_isUserSelectable(),
                                                                                 referenceAttribute.internal_isUserCreatable(),
                                                                                 referenceAttribute.internal_isUserDeletable(),
                                                                                 referenceAttribute.internal_isCatalogueBased()),
                                             uniformDesign);

            }
        });

        result.add(new SingleAttributeEditorBuilder<List<Data>>(){
            @Override
            public boolean isEditorFor(Attribute<?, ?> attribute) {
                return attribute instanceof ReferenceListAttribute;
            }

            @Override
            @SuppressWarnings("unchecked")
            public AttributeEditor<List<Data>, ?> createEditor(Attribute<?, ?> attribute, Consumer<Data> navigateToData, Data previousData) {
                ReferenceListAttribute referenceListAttribute = (ReferenceListAttribute)attribute;
                if(referenceListAttribute.internal_isCatalogueBased()){
                    return new AttributeEditor<>(referenceListAttribute, new CatalogListAttributeVisualisation(referenceListAttribute::internal_possibleValues, referenceListAttribute), uniformDesign);
                } else {
                    final TableView<Data> dataTableView = new TableView<>();
                    final ReferenceListAttributeVisualisation referenceListAttributeVisualisation = new ReferenceListAttributeVisualisation(uniformDesign, navigateToData, dataTableView, new ReferenceListAttributeEditWidget<Data>(dataTableView, navigateToData, uniformDesign, referenceListAttribute));
                    ExpandableAttributeVisualisation<List<Data>> expandableAttributeVisualisation = new ExpandableAttributeVisualisation<>(referenceListAttributeVisualisation, uniformDesign, (l) -> "Items: " + l.size(), FontAwesome.Glyph.LIST);
                    if (referenceListAttribute.contains(previousData)) {
                        expandableAttributeVisualisation.expand();
                    }
                    return new AttributeEditor<>(referenceListAttribute, expandableAttributeVisualisation, uniformDesign);
                }
            }
        });
        result.add(new NoListSingleAttributeEditorBuilder<>(uniformDesign,(attribute)->true,(attribute)->new DefaultValueAttributeVisualisation()));
        return result;
    }

    public AttributeEditor<?, ?> getAttributeEditor(Attribute<?, ?> attribute, Consumer<Data> navigateToData, Data oldValue) {
        if (attribute instanceof ValueListAttribute<?, ?>) {
            return singleAttributeEditorBuilders.stream().filter(a -> a.isListItemEditorFor(attribute)).findAny().orElseThrow(() -> new RuntimeException("No implementation found for " + attribute.getClass().getSimpleName()))
                                                .createValueListEditor(attribute);
        }
        return singleAttributeEditorBuilders.stream().filter(a -> a.isEditorFor(attribute)).findAny().orElseThrow(() -> new RuntimeException("No implementation found for " + attribute.getClass().getSimpleName()))
                                            .createEditor(attribute, navigateToData, oldValue);
    }
}