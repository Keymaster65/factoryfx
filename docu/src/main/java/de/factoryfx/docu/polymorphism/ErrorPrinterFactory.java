package de.factoryfx.docu.polymorphism;

import de.factoryfx.factory.PolymorphicFactoryBase;
import de.factoryfx.factory.SimpleFactoryBase;

public class ErrorPrinterFactory extends PolymorphicFactoryBase<Printer,Void> {

    @Override
    public Printer createImpl() {
        return new ErrorPrinter();
    }

    @Override
    public Class<Printer> getLiveObjectClass() {
        return Printer.class;
    }
}
