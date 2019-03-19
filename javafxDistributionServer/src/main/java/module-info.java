module io.github.factoryfx.javafxDistributionServer {
    requires io.github.factoryfx.data;
    requires io.github.factoryfx.factory;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.util;
    requires java.ws.rs;
    requires com.google.common;

    exports io.github.factoryfx.javafx.distribution.launcher.rest;
    exports io.github.factoryfx.javafx.distribution.server.rest;
}