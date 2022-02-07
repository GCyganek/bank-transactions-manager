package controller.sources;

import controller.TransactionsManagerAppController;
import javafx.stage.Stage;
import watcher.SourceObserver;
import watcher.exceptions.InvalidSourceConfigException;

import java.util.Optional;

public interface SourceAdditionWindowController {
    Optional<SourceObserver> getAddedSourceObserver() throws InvalidSourceConfigException;
    void setStage(Stage stage);
    void setAppController(TransactionsManagerAppController transactionsManagerAppController);
}
