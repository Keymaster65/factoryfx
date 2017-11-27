package de.factoryfx.server;

public class ApplicationServerTest {

    //TODO
//    @Test
//    @SuppressWarnings("unchecked")
//    public void test_getDiffForFactory_simple() throws Exception {
//        final ExampleFactoryA root = new ExampleFactoryA();
//        final InMemoryFactoryStorage<Void, ExampleLiveObjectA, ExampleFactoryA> memoryFactoryStorage = new InMemoryFactoryStorage<>(root);
//        memoryFactoryStorage.loadInitialFactory();
//        Thread.sleep(2);//avoid same timestamp
//        {
//            final FactoryAndNewMetadata<ExampleFactoryA> prepareNewFactory = memoryFactoryStorage.getPrepareNewFactory();
//            prepareNewFactory.root.stringAttribute.set("change1");
//            memoryFactoryStorage.updateCurrentFactory(prepareNewFactory, "user", "comment1");
//        }
//        Thread.sleep(2);//avoid same timestamp
//        {
//            final FactoryAndNewMetadata<ExampleFactoryA> prepareNewFactory = memoryFactoryStorage.getPrepareNewFactory();
//            prepareNewFactory.root.stringAttribute.set("change2");
//            memoryFactoryStorage.updateCurrentFactory(prepareNewFactory, "user", "comment2");
//        }
//        Thread.sleep(2);//avoid same timestamp
//        {
//            final FactoryAndNewMetadata<ExampleFactoryA> prepareNewFactory = memoryFactoryStorage.getPrepareNewFactory();
//            prepareNewFactory.root.stringAttribute.set("change3");
//            memoryFactoryStorage.updateCurrentFactory(prepareNewFactory, "user", "comment3");
//        }
//        Thread.sleep(2);//avoid same timestamp
//
//        ApplicationServer<Void,ExampleLiveObjectA,ExampleFactoryA> applicationServer = new ApplicationServer<>(Mockito.mock(FactoryManager.class), memoryFactoryStorage);
//
//        final List<AttributeDiffInfo> diff = applicationServer.getDiffHistoryForFactory(root.getId());
//        Assert.assertEquals(3,diff.size());
//        Assert.assertEquals("change3",diff.get(0).getAttributeDisplayText());
//        Assert.assertEquals("change2",diff.get(1).getAttributeDisplayText());
//        Assert.assertEquals("change1",diff.get(2).getAttributeDisplayText());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    public void test_getDiffForFactory_other_change() throws Exception {
//        final ExampleFactoryA root = new ExampleFactoryA();
//        root.referenceAttribute.set(new ExampleFactoryB());
//        final InMemoryFactoryStorage<Void, ExampleLiveObjectA, ExampleFactoryA> memoryFactoryStorage = new InMemoryFactoryStorage<>(root);
//        memoryFactoryStorage.loadInitialFactory();
//        Thread.sleep(2);//avoid same timestamp
//        {
//            final FactoryAndNewMetadata<ExampleFactoryA> prepareNewFactory = memoryFactoryStorage.getPrepareNewFactory();
//            prepareNewFactory.root.stringAttribute.set("change1");
//            memoryFactoryStorage.updateCurrentFactory(prepareNewFactory, "user", "comment1");
//        }
//        Thread.sleep(2);//avoid same timestamp
//        {
//            final FactoryAndNewMetadata<ExampleFactoryA> prepareNewFactory = memoryFactoryStorage.getPrepareNewFactory();
//            prepareNewFactory.root.stringAttribute.set("change2");
//            memoryFactoryStorage.updateCurrentFactory(prepareNewFactory, "user", "comment2");
//        }
//        Thread.sleep(2);//avoid same timestamp
//        {//change other factory
//            final FactoryAndNewMetadata<ExampleFactoryA> prepareNewFactory = memoryFactoryStorage.getPrepareNewFactory();
//            root.referenceAttribute.get().stringAttribute.set("different change");
//            memoryFactoryStorage.updateCurrentFactory(prepareNewFactory, "user", "comment2");
//        }
//        Thread.sleep(2);//avoid same timestamp
//        {
//            final FactoryAndNewMetadata<ExampleFactoryA> prepareNewFactory = memoryFactoryStorage.getPrepareNewFactory();
//            prepareNewFactory.root.stringAttribute.set("change3");
//            memoryFactoryStorage.updateCurrentFactory(prepareNewFactory, "user", "comment3");
//        }
//        Thread.sleep(2);//avoid same timestamp
//
//        ApplicationServer<Void,ExampleLiveObjectA,ExampleFactoryA> applicationServer = new ApplicationServer<>(Mockito.mock(FactoryManager.class), memoryFactoryStorage);
//
//        final List<AttributeDiffInfo> diff = applicationServer.getDiffHistoryForFactory(root.getId());
//        Assert.assertEquals(3,diff.size());
//        Assert.assertEquals("change3",diff.get(0).getNewAttributeDisplayText());
//        Assert.assertEquals("change2",diff.get(1).getNewAttributeDisplayText());
//        final String displayText = diff.get(2).getNewAttributeDisplayText();
//        if (!"change1".equals(displayText)){
//            System.out.println("ssfd");
//        }
//        Assert.assertEquals("change1", displayText);
//    }
//
//    @Test
//    public void testUpdateReferenceList() {
//        final ExampleFactoryA root = new ExampleFactoryA();
//        root.referenceListAttribute.add(new ExampleFactoryB());
//        final InMemoryFactoryStorage<Void, ExampleLiveObjectA, ExampleFactoryA> memoryFactoryStorage = new InMemoryFactoryStorage<>(root);
//        memoryFactoryStorage.loadInitialFactory();
//        ApplicationServer<Void,ExampleLiveObjectA,ExampleFactoryA> applicationServer = new ApplicationServer<>(new FactoryManager<>(new RethrowingFactoryExceptionHandler<>()), memoryFactoryStorage);
//        applicationServer.start();
//        FactoryAndNewMetadata<ExampleFactoryA> editableFactory = applicationServer.prepareNewFactory();
//        editableFactory.root.referenceListAttribute.add(new ExampleFactoryB());
//        FactoryUpdateLog log = applicationServer.updateCurrentFactory(editableFactory,"","", x->true);
//        AttributeDiffInfo theDiff = log.mergeDiffInfo.mergeInfos.get(0);
//        String dt = theDiff.getPreviousAttributeDisplayText();
//        String dtNew = theDiff.getNewAttributeDisplayText();
//        Assert.assertNotEquals(dt,dtNew);
//
//    }
//
//    @Test
//    public void testUpdateReferenceListTwice() throws InterruptedException {
//        final ExampleFactoryA root = new ExampleFactoryA();
//        root.referenceListAttribute.add(new ExampleFactoryB());
//        final InMemoryFactoryStorage<Void, ExampleLiveObjectA, ExampleFactoryA> memoryFactoryStorage = new InMemoryFactoryStorage<>(root);
//        memoryFactoryStorage.loadInitialFactory();
//        ApplicationServer<Void,ExampleLiveObjectA,ExampleFactoryA> applicationServer = new ApplicationServer<>(new FactoryManager<>(new RethrowingFactoryExceptionHandler<>()), memoryFactoryStorage);
//        applicationServer.start();
//        FactoryAndNewMetadata<ExampleFactoryA> editableFactory = applicationServer.prepareNewFactory();
//        Thread.sleep(10);
//        editableFactory.root.referenceListAttribute.add(new ExampleFactoryB());
//        FactoryUpdateLog log = applicationServer.updateCurrentFactory(editableFactory,"","", x->true);
//        editableFactory = applicationServer.prepareNewFactory();
//        Thread.sleep(10);
//        editableFactory.root.referenceListAttribute.add(new ExampleFactoryB());
//        log = applicationServer.updateCurrentFactory(editableFactory,"","", x->true);
//        Assert.assertTrue(log.mergeDiffInfo.hasNoConflicts());
//        editableFactory = applicationServer.prepareNewFactory();
//        Thread.sleep(10);
//        editableFactory.root.referenceListAttribute.remove(editableFactory.root.referenceListAttribute.get(0));
//        log = applicationServer.updateCurrentFactory(editableFactory,"","", x->true);
//        Assert.assertTrue(log.mergeDiffInfo.hasNoConflicts());
//    }

}