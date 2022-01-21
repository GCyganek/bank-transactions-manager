package controller.sources.util;

import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.util.BankType;
import watcher.SourceObserver;

public record SourceTable(TableView<SourceObserver> tableView,
                          TableColumn<SourceObserver, String> descriptionColumn,
                          TableColumn<SourceObserver, BankType> bankTypeColumn,
                          TableColumn<SourceObserver, Boolean> activeColumn,
                          Button deleteButton, Button reactivateButton, Button deactivateButton) {
}
