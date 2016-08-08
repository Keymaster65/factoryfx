package de.factoryfx.development.angularjs.integration;

import java.util.ArrayList;
import java.util.Optional;

import de.factoryfx.factory.FactoryBase;
import de.factoryfx.factory.attribute.AttributeMetadata;
import de.factoryfx.factory.attribute.util.BigDecimalAttribute;
import de.factoryfx.factory.attribute.util.BooleanAttribute;
import de.factoryfx.factory.attribute.util.DoubleAttribute;
import de.factoryfx.factory.attribute.util.EnumAttribute;
import de.factoryfx.factory.attribute.util.IntegerAttribute;
import de.factoryfx.factory.attribute.util.LongAttribute;
import de.factoryfx.factory.attribute.ReferenceAttribute;
import de.factoryfx.factory.attribute.ReferenceListAttribute;
import de.factoryfx.factory.attribute.util.StringAttribute;
import de.factoryfx.factory.attribute.util.StringListAttribute;
import de.factoryfx.factory.attribute.util.StringMapAttribute;
import de.factoryfx.factory.validation.StringRequired;

public class ExampleFactoryA extends FactoryBase<ExampleLiveObjectA,ExampleFactoryA> {

    public final StringAttribute stringAttribute=new StringAttribute(new AttributeMetadata().en("StringAttribute").de("StringAttribute de").permission(Permissions.PERMISSON_X)).validation(new StringRequired());
    public final BigDecimalAttribute bigDecimalAttribute=new BigDecimalAttribute(new AttributeMetadata().en("BigDecimalAttribute").de("BigDecimalAttribute de"));
    public final BooleanAttribute booleanAttribute=new BooleanAttribute(new AttributeMetadata().en("BooleanAttribute").de("BooleanAttribute de"));
    public final DoubleAttribute doubleAttribute=new DoubleAttribute(new AttributeMetadata().en("DoubleAttribute").de("DoubleAttribute de"));
    public final EnumAttribute<ExampleEnum> enumAttribute=new EnumAttribute<>(ExampleEnum.class,new AttributeMetadata().en("EnumAttribute").de("EnumAttribute de"));
    public final IntegerAttribute integerAttribute=new IntegerAttribute(new AttributeMetadata().en("IntegerAttribute").de("IntegerAttribute de"));
    public final LongAttribute longAttribute=new LongAttribute(new AttributeMetadata().en("LongAttribute").de("LongAttribute de"));
    public final StringListAttribute valueListAttribute=new StringListAttribute(new AttributeMetadata().en("ValueListAttribute").de("ValueListAttribute de"));
    public final StringMapAttribute mapAttribute=new StringMapAttribute(new AttributeMetadata().en("MapAttribute").de("MapAttribute de"));

    public final ReferenceAttribute<ExampleFactoryB> referenceAttribute = new ReferenceAttribute<>(ExampleFactoryB.class,new AttributeMetadata().en("ReferenceAttribute").de("ReferenceAttribute de"));
    public final ReferenceListAttribute<ExampleFactoryB> referenceListAttribute = new ReferenceListAttribute<>(ExampleFactoryB.class,new AttributeMetadata().en("ReferenceListAttribute").de("ReferenceListAttribute de"));

    @Override
    protected ExampleLiveObjectA createImp(Optional<ExampleLiveObjectA> previousLiveObject) {
        ArrayList<ExampleLiveObjectB> exampleLiveObjectBs = new ArrayList<>();
        referenceListAttribute.get().forEach(exampleFactoryB -> {
            exampleLiveObjectBs.add(exampleFactoryB.create());
        });

        return new ExampleLiveObjectA(referenceAttribute.get().create(), exampleLiveObjectBs);
    }
}