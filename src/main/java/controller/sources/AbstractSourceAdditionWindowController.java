package controller.sources;

import controller.TransactionsManagerAppController;
import javafx.stage.Stage;
import model.util.BankType;
import model.util.SourceType;
import watcher.SourceObserver;
import watcher.builder.SourceObserverBuilder;
import watcher.exceptions.InvalidSourceConfigException;

import java.util.Optional;

public abstract class AbstractSourceAdditionWindowController implements SourceAdditionWindowController {

    protected Stage stage;
    protected TransactionsManagerAppController appController;

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    protected Optional<SourceObserver> buildAddedSourceObserver(SourceType sourceType, BankType bankType, String uri)
            throws InvalidSourceConfigException {
        SourceObserver sourceObserver = SourceObserverBuilder.with()
                .withSourceType(sourceType)
                .withBankType(bankType)
                .withDescription(uri)
                .build();

        return Optional.of(sourceObserver);
    }
}
