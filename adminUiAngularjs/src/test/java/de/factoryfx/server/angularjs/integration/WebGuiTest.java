package de.factoryfx.server.angularjs.integration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.io.ByteStreams;
import de.factoryfx.factory.FactoryManager;
import de.factoryfx.data.storage.DataStorage;
import de.factoryfx.data.storage.inmemory.InMemoryDataStorage;
import de.factoryfx.factory.exception.RethrowingFactoryExceptionHandler;
import de.factoryfx.factory.util.ClasspathBasedFactoryProvider;
import de.factoryfx.server.ApplicationServer;
import de.factoryfx.server.angularjs.WebGuiApplicationCreator;
import de.factoryfx.server.angularjs.factory.SessionStorageFactory;
import de.factoryfx.server.angularjs.factory.server.HttpServer;
import de.factoryfx.server.angularjs.factory.server.HttpServerFactory;
import de.factoryfx.server.angularjs.factory.server.resourcehandler.ConfigurableResourceHandler;
import de.factoryfx.server.angularjs.factory.server.resourcehandler.FilesystemFileContentProvider;
import de.factoryfx.server.angularjs.integration.example.ExampleFactoryA;
import de.factoryfx.server.angularjs.integration.example.ExampleFactoryB;
import de.factoryfx.server.angularjs.integration.example.ExampleLiveObjectA;
import de.factoryfx.server.angularjs.integration.example.ExampleVisitor;
import de.factoryfx.server.angularjs.integration.example.ViewCreator;
import de.factoryfx.server.angularjs.integration.example.VisitorToTables;
import de.factoryfx.testutils.SingleProcessInstanceUtil;
import de.factoryfx.testutils.WebAppViewer;
import de.factoryfx.user.nop.NoUserManagement;
import de.factoryfx.user.UserManagement;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

public class WebGuiTest extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        new WebAppViewer(primaryStage, () -> {
            ExampleFactoryA exampleFactoryA = new ExampleFactoryA();
            exampleFactoryA.stringAttribute.set("balblub");
            ExampleFactoryB exampleFactoryB = new ExampleFactoryB();
            exampleFactoryB.stringAttribute.set("BBBBBBBBBBBBBBBB");
            exampleFactoryA.referenceAttribute.set(exampleFactoryB);
            exampleFactoryA.valueListAttribute.add("a111111");
            exampleFactoryA.valueListAttribute.add("b222222");

            exampleFactoryA.mapAttribute.get().put("key1","value1");
            exampleFactoryA.mapAttribute.get().put("key2","value2");
            exampleFactoryA.mapAttribute.get().put("key3","value3");

            exampleFactoryA.byteArrayAttribute.set(new byte[]{1,2,3,4,5});

            for (int i=0; i<3;i++){
                ExampleFactoryB value = new ExampleFactoryB();
                value.stringAttribute.set("i"+i);
                exampleFactoryA.referenceListAttribute.add(value);
            }
            exampleFactoryA.referenceListAttribute.add(exampleFactoryA.referenceAttribute.get());

            ApplicationServer<ExampleVisitor, ExampleLiveObjectA, ExampleFactoryA,Void> exampleApplicationServer = new ApplicationServer<>(new FactoryManager<>(new RethrowingFactoryExceptionHandler<>()), new InMemoryDataStorage<>(exampleFactoryA));
            exampleApplicationServer.start();

            {
                WebGuiApplicationCreator<ExampleVisitor, ExampleLiveObjectA, ExampleFactoryA,Void> webGuiApplicationCreator = new WebGuiApplicationCreator<>(exampleApplicationServer, Arrays.asList(ExampleFactoryA.class, ExampleFactoryB.class), getUserManagement(), () -> new ExampleVisitor(), new VisitorToTables(), new ViewCreator().create());
                HttpServerFactory<ExampleVisitor, ExampleLiveObjectA, ExampleFactoryA,Void> defaultFactory = webGuiApplicationCreator.createDefaultFactory();
                if (WebGuiApplicationCreator.class.getResourceAsStream("/logo/logoLarge.png") != null) {
                    try (InputStream inputStream = WebGuiApplicationCreator.class.getResourceAsStream("/logo/logoLarge.png")) {
                        defaultFactory.webGuiResource.get().layout.get().logoLarge.set(ByteStreams.toByteArray(inputStream));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (WebGuiApplicationCreator.class.getResourceAsStream("/logo/logoSmall.png") != null) {
                    try (InputStream inputStream = WebGuiApplicationCreator.class.getResourceAsStream("/logo/logoSmall.png")) {
                        defaultFactory.webGuiResource.get().layout.get().logoSmall.set(ByteStreams.toByteArray(inputStream));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                defaultFactory.resourceHandler.set(new ConfigurableResourceHandler(new FilesystemFileContentProvider(Paths.get("./src/main/resources/webapp"), "body {background-color: inherited;}".getBytes(StandardCharsets.UTF_8)), () -> UUID.randomUUID().toString()));
                DataStorage<HttpServerFactory<ExampleVisitor, ExampleLiveObjectA, ExampleFactoryA,Void>,Void> lvtInMemoryDataStorage = new InMemoryDataStorage<>(defaultFactory);
                ApplicationServer<Void,HttpServer,HttpServerFactory<ExampleVisitor, ExampleLiveObjectA, ExampleFactoryA,Void>,Void> exampleServer = webGuiApplicationCreator.createApplication(lvtInMemoryDataStorage);
                exampleServer.start();

                {
                    WebGuiApplicationCreator<Void,HttpServer,HttpServerFactory<ExampleVisitor, ExampleLiveObjectA, ExampleFactoryA,Void>,Void> selfServerCreator = new WebGuiApplicationCreator<>(exampleServer, new ClasspathBasedFactoryProvider().get(SessionStorageFactory.class), new NoUserManagement(), null, null, Collections.emptyList());
                    HttpServerFactory<Void,HttpServer,HttpServerFactory<ExampleVisitor, ExampleLiveObjectA, ExampleFactoryA,Void>,Void> selfDefaultFactory = selfServerCreator.createDefaultFactory();
                    selfDefaultFactory.port.set(8087);
                    ApplicationServer<Void, HttpServer, HttpServerFactory<Void, HttpServer, HttpServerFactory<ExampleVisitor, ExampleLiveObjectA, ExampleFactoryA,Void>,Void>,Void> selfServer = selfServerCreator.createApplication(new InMemoryDataStorage<>(selfDefaultFactory));
                    selfServer.start();
                }

            }

        },"http://localhost:8087/#/login","http://localhost:8089/#/login");
    }

    protected UserManagement getUserManagement() {
        return new NoUserManagement();
    }

    public static void main(String[] args) {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        SingleProcessInstanceUtil.enforceSingleProcessInstance(37453);
        Application.launch(args);
    }

}