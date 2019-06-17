package io.github.factoryfx.docu.dependencyinjection;

import io.github.factoryfx.factory.SimpleFactoryBase;

public class DependencyFactory extends SimpleFactoryBase<Dependency,RootFactory> {
    @Override
    protected Dependency createImpl() {
        return new Dependency();
    }
}
