package de.factoryfx.javafx.data.widget;

import javafx.scene.Node;

public interface CloseAwareWidget {
    void closeNotifier();
    Node createContent();
}