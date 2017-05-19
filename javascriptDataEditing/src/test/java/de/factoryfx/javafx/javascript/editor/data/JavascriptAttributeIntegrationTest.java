package de.factoryfx.javafx.javascript.editor.data;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;

import de.factoryfx.data.attribute.Attribute;
import de.factoryfx.data.jackson.ObjectMapperBuilder;
import de.factoryfx.javafx.editor.attribute.AttributeEditor;
import de.factoryfx.javafx.editor.attribute.AttributeEditorBuilder;
import de.factoryfx.javafx.editor.data.DataEditor;
import de.factoryfx.javafx.javascript.editor.attribute.visualisation.JavascriptAttributeVisualisation;
import de.factoryfx.javafx.util.UniformDesign;
import de.factoryfx.javascript.data.attributes.types.Javascript;
import de.factoryfx.javascript.data.attributes.types.JavascriptAttribute;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JavascriptAttributeIntegrationTest extends Application{

    @Override
    @SuppressWarnings("unchecked")
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/de/factoryfx/javafx/css/app.css").toExternalForm());



        ExampleJavascript exampleJavascript = new ExampleJavascript();


        final ArrayList<BiFunction<UniformDesign,Attribute<?>, Optional<AttributeEditor<?>>>> editorAssociations = new ArrayList<>();
        editorAssociations.add((uniformDesign, a)->{
            if (Javascript.class==a.internal_getAttributeType().dataType){
                JavascriptAttribute javascriptAttribute = (JavascriptAttribute) a;
                return Optional.of(new AttributeEditor<>(javascriptAttribute,new JavascriptAttributeVisualisation(javascriptAttribute),uniformDesign));
            }

            return Optional.empty();
        });
        UniformDesign uniformDesign = new UniformDesign(Locale.ENGLISH, Color.web("#FF7979"),Color.web("#F0AD4E"),Color.web("#5BC0DE"),Color.web("#5CB85C"),Color.web("#5494CB"),Color.web("#B5B5B5"),false);
        AttributeEditorBuilder attributeEditorBuilder = new AttributeEditorBuilder(uniformDesign, editorAssociations);


        DataEditor dataEditor = new DataEditor(attributeEditorBuilder,uniformDesign);
        root.setCenter(dataEditor.createContent());

        exampleJavascript= ObjectMapperBuilder.build().copy(exampleJavascript);

        exampleJavascript = exampleJavascript.internal().prepareUsableCopy();
        dataEditor.edit(exampleJavascript);

        primaryStage.setScene(new Scene(root,1200,800));

        Button gc = new Button("gc");
        gc.setOnAction(event -> System.gc());

        Button exec = new Button("execute");
        ExampleJavascript pExampleJavascript = exampleJavascript;
        exec.setOnAction(event -> pExampleJavascript.specialAttribute.get().execute(System.out));

        HBox buttons = new HBox();
        buttons.getChildren().add(gc);
        buttons.getChildren().add(exec);
        root.setBottom(buttons);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());

        Application.launch();
    }
}