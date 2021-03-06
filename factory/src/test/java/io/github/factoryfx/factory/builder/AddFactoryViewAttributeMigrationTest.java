package io.github.factoryfx.factory.builder;

import io.github.factoryfx.factory.SimpleFactoryBase;
import io.github.factoryfx.factory.attribute.dependency.FactoryAttribute;
import io.github.factoryfx.factory.attribute.dependency.FactoryViewAttribute;
import io.github.factoryfx.factory.attribute.types.StringAttribute;
import io.github.factoryfx.factory.jackson.ObjectMapperBuilder;
import io.github.factoryfx.factory.storage.DataUpdate;
import io.github.factoryfx.server.Microservice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AddFactoryViewAttributeMigrationTest {

    //----------------------------------old
    public static class ServerFactoryOld extends SimpleFactoryBase<Void, ServerFactoryOld> {

        public final FactoryAttribute<Void, NestedFactoryOld> nestedFactory = new FactoryAttribute<>();
        public final FactoryAttribute<Void, ViewFactoryOld> view = new FactoryAttribute<>();

        @Override
        protected Void createImpl() {
            return null;
        }
    }


    public static class ViewFactoryOld extends SimpleFactoryBase<Void, ServerFactoryOld> {

        public final StringAttribute stringAttribute = new StringAttribute();

        @Override
        protected Void createImpl() {
            return null;
        }
    }

    public static class NestedFactoryOld extends SimpleFactoryBase<Void, ServerFactoryOld> {
        @Override
        protected Void createImpl() {
            return null;
        }
    }


    //----------------------------------new
    public static class ServerFactory extends SimpleFactoryBase<Void, ServerFactory> {
        public final FactoryAttribute<Void, NestedFactory> nestedFactory = new FactoryAttribute<>();
        public final FactoryAttribute<Void, ViewFactory> view = new FactoryAttribute<>();

        @Override
        protected Void createImpl() {
            return null;
        }
    }

    public static class NestedFactory extends SimpleFactoryBase<Void, ServerFactory> {

        public final FactoryViewAttribute<ServerFactory, Void, ViewFactory> view = new FactoryViewAttribute<>(s -> s.view.get());

        @Override
        protected Void createImpl() {
            return null;
        }
    }

    public static class ViewFactory extends SimpleFactoryBase<Void, ServerFactory> {

        public final StringAttribute stringAttribute = new StringAttribute();

        @Override
        protected Void createImpl() {
            return null;
        }
    }

    @TempDir
    public Path folder;

    @Test
    public void test_no_null_pointer_exception() throws IOException {
        {
            FactoryTreeBuilder<Void, ServerFactoryOld> builder = new FactoryTreeBuilder<>(ServerFactoryOld.class, ctx -> {
                ServerFactoryOld serverFactory = new ServerFactoryOld();
                serverFactory.nestedFactory.set(ctx.get(NestedFactoryOld.class));
                serverFactory.view.set(ctx.get(ViewFactoryOld.class));
                return serverFactory;
            });

            builder.addFactory(NestedFactoryOld.class, Scope.SINGLETON, ctx -> new NestedFactoryOld());
            builder.addFactory(ViewFactoryOld.class, Scope.SINGLETON, ctx -> {
                ViewFactoryOld viewFactory = new ViewFactoryOld();
                viewFactory.stringAttribute.set("string value");
                return viewFactory;
            });

            System.out.println(ObjectMapperBuilder.build().writeValueAsString(builder.buildTree().internal().createDataStorageMetadataDictionaryFromRoot()));


            Microservice<Void, ServerFactoryOld> msOld = builder.microservice().withFilesystemStorage(folder).build();
            msOld.start();
            msOld.stop();
        }

        //Patch class names in json files
        String currentFactory=Files.readString(folder.resolve("currentFactory.json"));
        currentFactory=currentFactory.replace("Old","");
        Files.writeString(folder.resolve("currentFactory.json"),currentFactory);
        String currentFactorymetadata=Files.readString(folder.resolve("currentFactory_metadata.json"));
        currentFactorymetadata=currentFactorymetadata.replace("Old","");
        Files.writeString(folder.resolve("currentFactory_metadata.json"),currentFactorymetadata);
        for (File file : folder.resolve("history").toFile().listFiles()) {
            String historyFactory=Files.readString(file.toPath());
            historyFactory=historyFactory.replace("Old","");
            Files.writeString(file.toPath(),historyFactory);
        }



        {
            FactoryTreeBuilder<Void, ServerFactory> builder = new FactoryTreeBuilder<>(ServerFactory.class, ctx -> {
                ServerFactory serverFactory = new ServerFactory();
                serverFactory.nestedFactory.set(ctx.get(NestedFactory.class));
                serverFactory.view.set(ctx.get(ViewFactory.class));
                return serverFactory;
            });

            builder.addFactory(NestedFactory.class, Scope.SINGLETON, ctx -> new NestedFactory());
            builder.addFactory(ViewFactory.class, Scope.SINGLETON, ctx -> {
                ViewFactory viewFactory = new ViewFactory();
                viewFactory.stringAttribute.set("string value");
                return viewFactory;
            });

            Microservice<Void, ServerFactory> msNew = builder.microservice().withFilesystemStorage(folder).build();
            msNew.start();

            DataUpdate<ServerFactory> update = msNew.prepareNewFactory();
            msNew.updateCurrentFactory(update);

            msNew.stop();
        }
    }
}
