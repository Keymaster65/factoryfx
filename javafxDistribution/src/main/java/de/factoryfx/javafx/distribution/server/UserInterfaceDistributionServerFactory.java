package de.factoryfx.javafx.distribution.server;

import java.io.File;

import de.factoryfx.data.attribute.AttributeMetadata;
import de.factoryfx.data.attribute.types.IntegerAttribute;
import de.factoryfx.data.attribute.types.StringAttribute;
import de.factoryfx.factory.FactoryBase;
import de.factoryfx.javafx.distribution.server.rest.DownloadResource;

public class UserInterfaceDistributionServerFactory<V> extends FactoryBase<UserInterfaceDistributionServer,V> {
    public UserInterfaceDistributionServerFactory(){
        config().setDisplayTextProvider(() -> "http://"+host.get()+":"+port.get());

        configLiveCycle().setCreator(() -> new UserInterfaceDistributionServer(host.get(),port.get(),new DownloadResource(new File(guiZipFile.get()))));
        configLiveCycle().setStarter(newLiveObject -> newLiveObject.start());
        configLiveCycle().setDestroyer(previousLiveObject -> previousLiveObject.stop());
    }

    public final StringAttribute host = new StringAttribute(new AttributeMetadata().de("host").en("host"));
    public final IntegerAttribute port = new IntegerAttribute(new AttributeMetadata().de("port").en("port"));
    public final StringAttribute guiZipFile = new StringAttribute(new AttributeMetadata().de("Datei für UI").en("File containing UI"));

}
