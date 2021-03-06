package io.github.factoryfx.javafx.stage;

import io.github.factoryfx.factory.attribute.primitive.IntegerAttribute;
import io.github.factoryfx.factory.attribute.types.ObjectValueAttribute;
import io.github.factoryfx.factory.attribute.types.StringAttribute;
import io.github.factoryfx.factory.attribute.types.StringListAttribute;
import io.github.factoryfx.factory.FactoryBase;
import io.github.factoryfx.factory.attribute.dependency.FactoryAttribute;
import io.github.factoryfx.factory.attribute.dependency.FactoryListAttribute;
import io.github.factoryfx.javafx.css.CssUtil;
import io.github.factoryfx.javafx.RichClientRoot;
import io.github.factoryfx.javafx.util.LongRunningActionExecutor;
import io.github.factoryfx.javafx.util.LongRunningActionExecutorFactory;
import io.github.factoryfx.javafx.view.container.ViewsDisplayWidget;
import io.github.factoryfx.javafx.view.container.ViewsDisplayWidgetFactory;
import io.github.factoryfx.javafx.view.menu.ViewMenuFactory;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 */
public class StageFactory extends FactoryBase<Stage,RichClientRoot> {
    public final ObjectValueAttribute<Stage> stage = new ObjectValueAttribute<Stage>().en("main stage");
    public final FactoryListAttribute<Menu,ViewMenuFactory> items = new FactoryListAttribute<Menu,ViewMenuFactory>().de("items").en("items");
    public final FactoryAttribute<ViewsDisplayWidget,ViewsDisplayWidgetFactory> viewsDisplayWidget =new FactoryAttribute<ViewsDisplayWidget,ViewsDisplayWidgetFactory>().de("viewsDisplayWidget").en("viewsDisplayWidget");
    public final IntegerAttribute width = new IntegerAttribute().de("width").en("width");
    public final IntegerAttribute height = new IntegerAttribute().de("height").en("height");
    public final FactoryAttribute<LongRunningActionExecutor,LongRunningActionExecutorFactory> longRunningActionExecutor =new FactoryAttribute<LongRunningActionExecutor,LongRunningActionExecutorFactory>().de("longRunningActionExecutor").en("longRunningActionExecutor");
    public final StringListAttribute cssResourceUrlExternalForm = new StringListAttribute().de("cssResourceUrlExternalForm").en("cssResourceUrlExternalForm");
    public final StringAttribute title = new StringAttribute().de("title").en("title").nullable();

    public StageFactory(){
        cssResourceUrlExternalForm.add(CssUtil.getURL());

        configLifeCycle().setCreator(this::setupStage);
        configLifeCycle().setStarter((newLiveObject) -> stage.get().show());
        configLifeCycle().setDestroyer((previousLiveObject) -> stage.get().hide());
    }

    private Stage setupStage() {
        Stage stage = this.stage.get();

        BorderPane root = new BorderPane();
        root.setCenter(viewsDisplayWidget.instance().createContent());
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(items.instances());
        root.setTop(menuBar);

        LongRunningActionExecutor longRunningActionExecutor = this.longRunningActionExecutor.instance();


        for (String cssUrl: cssResourceUrlExternalForm){
            root.getStylesheets().add(cssUrl);
        }

        stage.setScene(new Scene(longRunningActionExecutor.wrap(root),width.get(),height.get()));
        stage.setTitle(title.get());


        return stage;
    }
}
