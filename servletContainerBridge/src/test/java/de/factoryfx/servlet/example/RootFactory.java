package de.factoryfx.servlet.example;

import de.factoryfx.data.attribute.types.StringAttribute;
import de.factoryfx.factory.SimpleFactoryBase;
import de.factoryfx.factory.atrribute.FactoryReferenceAttribute;
import de.factoryfx.server.rest.MicroserviceResourceFactory;
import de.factoryfx.servlet.ApplicationServerRestServletBridge;
import de.factoryfx.servlet.ApplicationServerRestServletBridgeFactory;
import de.factoryfx.servlet.ServletContextAwareVisitor;

public class RootFactory extends SimpleFactoryBase<Root,ServletContextAwareVisitor,RootFactory> {
    public final StringAttribute stringAttribute =new StringAttribute();
    public final FactoryReferenceAttribute<ApplicationServerRestServletBridge,ApplicationServerRestServletBridgeFactory<RootFactory,Void>> applicationServerRestBridge = new FactoryReferenceAttribute<ApplicationServerRestServletBridge,ApplicationServerRestServletBridgeFactory<RootFactory,Void>>().setupUnsafe(MicroserviceResourceFactory.class);


    @Override
    public Root createImpl() {
        return new Root();
    }
}
