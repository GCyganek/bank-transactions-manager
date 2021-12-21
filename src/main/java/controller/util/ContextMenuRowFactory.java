package controller.util;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.List;

public class ContextMenuRowFactory<T> implements Callback<TableView<T>, TableRow<T>> {

    private final ContextMenu menu;

    public ContextMenuRowFactory(ContextMenu menu) {
        this.menu = menu;
    }

    public List<MenuItem> getMenuItems() {
        return menu.getItems();
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        menu.getItems().setAll(menuItems);
    }

    @Override
    public TableRow<T> call(TableView<T> view) {
        final TableRow<T> row = new TableRow<>();

        row.contextMenuProperty().bind(
                Bindings.when(Bindings.isNotNull(row.itemProperty())).then(menu).otherwise((ContextMenu) null)
        );
        return row;
    }

}