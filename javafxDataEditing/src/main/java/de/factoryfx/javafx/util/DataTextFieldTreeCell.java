package de.factoryfx.javafx.util;

import java.util.Optional;
import java.util.function.Function;

import de.factoryfx.data.Data;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.cell.TextFieldTreeCell;

/** treeview cell for data with auto updatable displaytext*/
public class DataTextFieldTreeCell<T> extends TextFieldTreeCell<T> {

    private WeakChangeListener<String> changeListener;
    private Optional<ReadOnlyStringProperty> displayTextObservable=Optional.empty();
    private ChangeListener<String> changeListenerGarbageCollectionSave;
    private final Function<T,Data> dataSupplier;

    public DataTextFieldTreeCell(Function<T,Data> dataSupplier){
        this.dataSupplier = dataSupplier;
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            displayTextObservable.ifPresent(readOnlyStringProperty -> readOnlyStringProperty.removeListener(changeListener));
            displayTextObservable= Optional.of(dataSupplier.apply(item).internal().getDisplayTextObservable());
            changeListenerGarbageCollectionSave = (observable, oldValue, newValue) -> {
                if (item != null) {
                    setText(dataSupplier.apply(item).internal().getDisplayText());
                }
            };
            changeListener = new WeakChangeListener<>(changeListenerGarbageCollectionSave);
            displayTextObservable.get().addListener(changeListener);
            changeListener.changed(displayTextObservable.get(),null,displayTextObservable.get().get());
        }
        //CellUtils.updateItem(this, getConverter(), hbox, getTreeItemGraphic(), textField);
    }

}