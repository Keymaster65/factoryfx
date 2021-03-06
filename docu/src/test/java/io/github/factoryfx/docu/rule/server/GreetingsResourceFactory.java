package io.github.factoryfx.docu.rule.server;


import io.github.factoryfx.factory.attribute.dependency.FactoryAttribute;

public class GreetingsResourceFactory extends ServerBaseFactory<GreetingsResource> {

    public final FactoryAttribute<BackendClient, BackendClientFactory> backendClient = new FactoryAttribute<>();

    @Override
    protected GreetingsResource createImpl() {
        return new GreetingsResource(backendClient.instance());
    }

    public GreetingsResourceFactory(){
        this.config().setDisplayTextProvider(() -> "Greetings Resource");
    }
}
