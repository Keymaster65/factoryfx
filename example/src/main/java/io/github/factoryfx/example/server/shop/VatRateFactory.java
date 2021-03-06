package io.github.factoryfx.example.server.shop;

import io.github.factoryfx.factory.attribute.primitive.DoubleAttribute;
import io.github.factoryfx.example.server.ServerBaseFactory;

public class VatRateFactory extends ServerBaseFactory<VatRate> {

    public final DoubleAttribute rate= new DoubleAttribute().en("rate").addonText("%");

    public VatRateFactory(){
        config().setDisplayTextProvider(() -> "VatRate("+rate.get()+")");
    }

    @Override
    protected VatRate createImpl() {
        return new VatRate(rate.get());
    }

}
