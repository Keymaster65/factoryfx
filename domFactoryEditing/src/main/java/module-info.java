module io.github.factoryfx.domFactoryEditing {
    requires transitive io.github.factoryfx.microserviceRestServer;
    requires java.ws.rs;
    requires jersey.media.jaxb;
    requires org.eclipse.jetty.http;
    requires com.google.common;

    exports io.github.factoryfx.dom.rest;
}