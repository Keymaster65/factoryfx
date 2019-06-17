package io.github.factoryfx.docu.initializr;

import java.lang.Override;

/**
 * Factory for the example resource */
public class ExampleResourceFactory extends ServerBaseFactory<ExampleResource> {
    @Override
    protected ExampleResource createImpl() {
        return new ExampleResource();
    }
}
