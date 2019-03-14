package de.factoryfx.docu.customconfig;

import ch.qos.logback.classic.Level;
import de.factoryfx.factory.builder.FactoryTreeBuilder;
import de.factoryfx.factory.builder.Scope;
import de.factoryfx.jetty.JettyServerBuilder;
import de.factoryfx.jetty.JettyServerFactory;
import de.factoryfx.server.Microservice;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InterruptedException {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);

        FactoryTreeBuilder<Void, Server, ServerFactory,Void> builder = new FactoryTreeBuilder<>(ServerFactory.class);
        builder.addFactory(ServerFactory.class, Scope.SINGLETON);
        builder.addFactory(JettyServerFactory.class, Scope.SINGLETON, ctx-> new JettyServerBuilder<>(new JettyServerFactory<Void, ServerFactory>())
                .withHost("localhost").withPort(8005)
                .withResource(ctx.get(CustomConfigurationResourceFactory.class)).build());
        builder.addFactory(CustomConfigurationResourceFactory.class, Scope.SINGLETON);


        Microservice<Void, Server, ServerFactory,Void> microservice = builder.microservice().withInMemoryStorage().build();
        microservice.start();

        ping(8005);
        System.out.println("change port");
        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8005/CustomConfiguration/")).POST(HttpRequest.BodyPublishers.ofString(
                  "{ " +
                        "  \"port\": 8006 " +
                        "}"
        )).header("Content-Type", "application/json").build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
//            ignore exception cause connection is lost because we changed the port
        }

        Thread.sleep(1000);
        ping(8006);
    }

    private static void ping(int port){
        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:"+port+"/CustomConfiguration/")).GET().build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("ping "+port);
    }


}
