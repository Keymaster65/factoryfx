package de.factoryfx.jettyserver;

import java.util.Collection;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.factoryfx.data.merge.MergeDiff;
import de.factoryfx.factory.FactoryBase;
import de.factoryfx.factory.datastorage.FactoryAndStorageMetadata;
import de.factoryfx.factory.datastorage.StoredFactoryMetadata;
import de.factoryfx.server.ApplicationServer;

@Path("/") /** path defined in {@link de.factoryfx.xtc.ticketproxy.configuration.ConfigurationServer}*/
public class ApplicationServerResource<L,V,T extends FactoryBase<L,V>> implements ApplicationServer<L,V,T> {

    private final ApplicationServer<L,V,T> applicationServer;

    public ApplicationServerResource(ApplicationServer<L,V,T> applicationServer) {
        this.applicationServer = applicationServer;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateCurrentFactory")
    @Override
    public MergeDiff updateCurrentFactory(FactoryAndStorageMetadata<T> update, Locale locale) {
        return  applicationServer.updateCurrentFactory(update, locale);
    }

    @Override
    public MergeDiff simulateUpdateCurrentFactory(T updateFactory, String baseVersionId, Locale locale) {
        return applicationServer.simulateUpdateCurrentFactory(updateFactory,baseVersionId,locale);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("currentFactory")
    @Override
    public FactoryAndStorageMetadata<T> getCurrentFactory() {
        return applicationServer.getCurrentFactory();
    }

    @Override
    public FactoryAndStorageMetadata<T> getPrepareNewFactory() {
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("historyFactory")
    @Override
    public T getHistoryFactory(String id) {
        return applicationServer.getHistoryFactory(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("historyFactoryList")
    @Override
    public Collection<StoredFactoryMetadata> getHistoryFactoryList() {
        return applicationServer.getHistoryFactoryList();
    }

    @Override
    public void start() {
        applicationServer.start();
    }

    @Override
    public void stop() {
        applicationServer.stop();
    }


    @Override
    public V query(V visitor) {
        return applicationServer.query(visitor);
    }

}
