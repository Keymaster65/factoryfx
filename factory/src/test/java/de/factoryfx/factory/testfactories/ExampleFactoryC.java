package de.factoryfx.factory.testfactories;

import java.util.Optional;

import de.factoryfx.factory.FactoryBase;
import de.factoryfx.factory.attribute.AttributeMetadata;
import de.factoryfx.factory.attribute.StringAttribute;

public class ExampleFactoryC extends FactoryBase<ExampleLiveObjectC,ExampleFactoryC> {
    public final StringAttribute stringAttribute= new StringAttribute(new AttributeMetadata().labelText("ExampleB1"));

    @Override
    protected ExampleLiveObjectC createImp(Optional<ExampleLiveObjectC> previousLiveObject) {
        return new ExampleLiveObjectC();
    }
}
