package io.github.factoryfx.docu.datainjection;


import io.github.factoryfx.factory.builder.FactoryTreeBuilder;
import io.github.factoryfx.server.Microservice;

public class Main {

    public static void main(String[] args) {
        FactoryTreeBuilder<Root,RootFactory> builder = new FactoryTreeBuilder<>(RootFactory.class, ctx-> {
            RootFactory rootFactory = new RootFactory();
            rootFactory.text.set("Hello World");
            return rootFactory;
        });

        Microservice<Root,RootFactory> microservice = builder.microservice().build();
        microservice.start();
    }
}
