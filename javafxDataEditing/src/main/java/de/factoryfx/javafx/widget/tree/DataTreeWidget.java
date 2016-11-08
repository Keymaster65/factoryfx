package de.factoryfx.javafx.widget.tree;

import com.google.common.collect.TreeTraverser;
import de.factoryfx.data.Data;
import de.factoryfx.data.attribute.Attribute;
import de.factoryfx.data.attribute.ReferenceAttribute;
import de.factoryfx.data.attribute.ReferenceListAttribute;
import de.factoryfx.javafx.editor.data.DataEditor;
import de.factoryfx.javafx.util.UniformDesign;
import de.factoryfx.javafx.widget.CloseAwareWidget;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;

public class DataTreeWidget implements CloseAwareWidget {
    private final Data root;
    private final DataEditor dataEditor;
    private final UniformDesign uniformDesign;

    public DataTreeWidget(DataEditor dataEditor, Data root, UniformDesign uniformDesign) {
        this.dataEditor=dataEditor;
        this.root = root;
        this.uniformDesign = uniformDesign;
    }

    @Override
    public void closeNotifier() {
//        listener.changed(null, null, null);
    }

    @Override
    public Node createContent() {
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(dataEditor.createContent(),createTree());
        splitPane.setDividerPosition(0,0.75);
        return splitPane;
    }

    private Node createTree(){
        TreeView<TreeData> tree = new TreeView<>();
        tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tree.setCellFactory(param -> new TextFieldTreeCell<TreeData>() {
            @Override
            public void updateItem(TreeData item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    this.setText(item.getDisplayText());
                }
                //CellUtils.updateItem(this, getConverter(), hbox, getTreeItemGraphic(), textField);
            }

        });
        tree.setRoot(constructTree(root));

        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue!=null){
                dataEditor.edit(newValue.getValue().getData());
            }
        });

        ChangeListener<Data> dataChangeListener = (observable, oldValue, newValue) -> {
            Platform.runLater(() -> {//javafx bug workaround http://stackoverflow.com/questions/26343495/indexoutofboundsexception-while-updating-a-listview-in-javafx
                TreeItem<TreeData> treeItemRoot = constructTree(root);
                tree.setRoot(treeItemRoot);

                tree.getSelectionModel().clearSelection();
                for (TreeItem<TreeData> item : treeViewTraverser.breadthFirstTraversal(treeItemRoot)) {
                    if (item.getValue().match(newValue)) {
                        tree.getSelectionModel().select(item);
                    }
                }
            });

        };
        dataEditor.editData().addListener(dataChangeListener);
        dataChangeListener.changed(dataEditor.editData(),dataEditor.editData().get(),dataEditor.editData().get());

        ScrollPane scrollPane = new ScrollPane(tree);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("expand all");
        menuItem.setOnAction(event -> {
            for (TreeItem<TreeData> item : treeViewTraverser.breadthFirstTraversal(tree.getRoot())) {
                item.setExpanded(true);
            }
        });
        contextMenu.getItems().addAll(menuItem);
        tree.setContextMenu(contextMenu);

        return scrollPane;
    }

    TreeTraverser<TreeItem<TreeData>> treeViewTraverser = new TreeTraverser<TreeItem<TreeData>>() {
        @Override
        public Iterable<TreeItem<TreeData>> children(TreeItem<TreeData> data) {
            return data.getChildren();
        }
    };

    private TreeItem<TreeData> constructTree(Data data){
        if (data!=null){
            TreeItem<TreeData> dataTreeItem = new TreeItem<>(new TreeData(data,null));
            data.internal().visitAttributesFlat((attributeVariableName, attribute) -> {
                attribute.visit(new Attribute.AttributeVisitor() {
                    @Override
                    public void value(Attribute<?> value) {

                    }

                    @Override
                    public void reference(ReferenceAttribute<?> reference) {
                        TreeItem<TreeData> refDataTreeItem = new TreeItem<>(new TreeData(data,uniformDesign.getLabelText(reference)));
                        dataTreeItem.getChildren().add(refDataTreeItem);

                        final TreeItem<TreeData> treeItem = constructTree(reference.get());
                        if (treeItem!=null){
                            refDataTreeItem.getChildren().add(treeItem);
                        }
                    }

                    @Override
                    public void referenceList(ReferenceListAttribute<?> referenceList) {
                        TreeItem<TreeData> listDataTreeItem = new TreeItem<>(new TreeData(data,uniformDesign.getLabelText(referenceList)));
                        dataTreeItem.getChildren().add(listDataTreeItem);
                        referenceList.get().forEach((data)-> {
                            final TreeItem<TreeData> treeItem = constructTree(data);
                            if (treeItem!=null){
                                listDataTreeItem.getChildren().add(treeItem);
                            }
                        });
                    }
                });
            });
            return dataTreeItem;
        }
        return null;
    }

    public static class TreeData{
        private final Data data;
        private final String text;

        public TreeData(Data data, String text) {
            if (data==null){
                System.out.println();
            }
            this.data = data;
            this.text = text;
        }

        public String getDisplayText() {
            if (text==null && data!=null){
                return data.internal().getDisplayText();
            }
            return text;
        }


        public Data getData(){
            return data;
        }

        public boolean match(Data newValue) {
            return text==null && newValue==data;
        }
    }

}
