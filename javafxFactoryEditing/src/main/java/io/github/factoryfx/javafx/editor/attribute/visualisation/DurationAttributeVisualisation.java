package io.github.factoryfx.javafx.editor.attribute.visualisation;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import io.github.factoryfx.factory.attribute.time.DurationAttribute;
import io.github.factoryfx.javafx.editor.attribute.ValidationDecoration;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import com.google.common.base.Strings;

import io.github.factoryfx.javafx.editor.attribute.ValueAttributeVisualisation;
import io.github.factoryfx.javafx.editor.attribute.converter.DurationStringConverter;
import io.github.factoryfx.javafx.util.TypedTextFieldHelper;

public class DurationAttributeVisualisation extends ValueAttributeVisualisation<Duration, DurationAttribute> {

    public DurationAttributeVisualisation(DurationAttribute attribute, ValidationDecoration validationDecoration) {
        super(attribute, validationDecoration);
    }

    @Override
    public Node createValueVisualisation() {

        HBox hBox = new HBox(3);
        hBox.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label();
        label.textProperty().bindBidirectional(observableAttributeValue, new DurationStringConverter());

        ComboBox<ChronoUnit> comboBox = new ComboBox<>();
        comboBox.setEditable(false);
        comboBox.getItems().addAll(ChronoUnit.NANOS, ChronoUnit.MICROS, ChronoUnit.MILLIS, ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ChronoUnit.DAYS);

        TextField textField = new TextField();
        TypedTextFieldHelper.setupLongTextField(textField);
        textField.disableProperty().bind(comboBox.valueProperty().isNull());
        HBox.setHgrow(textField, Priority.ALWAYS);

        //        boundTo.addListener((observable, oldValue, newValue) -> {
        //            if(newValue == null || !newValue.equals(oldValue))
        //            setFields(newValue, textField, comboBox);
        //        });

        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> setDuration(observableAttributeValue, textField, comboBox));
        textField.textProperty().addListener((observable, oldValue, newValue) -> setDuration(observableAttributeValue, textField, comboBox));

        hBox.getChildren().addAll(label, comboBox, textField);
        hBox.disableProperty().bind(readOnly);
        return hBox;
    }

    private void setFields(Duration duration, TextField textField, ComboBox<ChronoUnit> comboBox) {
        if (duration != null) {
            textField.setText(String.valueOf(duration.get(ChronoUnit.SECONDS)));
            comboBox.setValue(ChronoUnit.SECONDS);
        } else {
            textField.setText(null);
            comboBox.setValue(null);
        }
    }

    private void setDuration(SimpleObjectProperty<Duration> boundTo, TextField textField, ComboBox<ChronoUnit> comboBox) {
        if (!Strings.isNullOrEmpty(textField.getText()) && comboBox.getValue() != null) {
            boundTo.set(Duration.of(Long.parseLong(textField.getText()), comboBox.getValue()));
        }
    }
}
