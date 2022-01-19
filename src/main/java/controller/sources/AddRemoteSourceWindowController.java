package controller.sources;

import controller.TransactionsManagerAppController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.util.BankType;
import watcher.SourceObserver;
import model.util.SourceType;
import watcher.builder.SourceObserverBuilderBuilder;
import watcher.exceptions.InvalidSourceConfigException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class AddRemoteSourceWindowController implements SourceAdditionWindowController {
    private Stage stage;

    private BankType bankType;

    private TransactionsManagerAppController appController;

    private final SimpleStringProperty remoteUrl = new SimpleStringProperty();

    @FXML
    public ChoiceBox<BankType> remoteBankChoiceBox;

    @FXML
    public Button addRemoteSourceButton;

    @FXML
    public TextField remoteSourceTextField;

    @FXML
    private void initialize() {
        remoteBankChoiceBox.getItems().addAll(BankType.values());
        remoteBankChoiceBox.setValue(BankType.SANTANDER);
        addRemoteSourceButton.disableProperty().bind(
                Bindings.length(remoteSourceTextField.textProperty()).isEqualTo(0)
                        .or(Bindings.isNull(remoteBankChoiceBox.valueProperty()))
        );
    }

    public void handleAddRemoteSourceButton(ActionEvent actionEvent) {
        remoteUrl.setValue(remoteSourceTextField.getText());
        bankType = remoteBankChoiceBox.getValue();
        stage.close();
    }

    private boolean validateUrl(String urlToValidate) {
        try {
            URL url = new URL(urlToValidate);
            url.toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            this.appController.showErrorWindow("Invalid remote source URL", e.getMessage());
            return false;
        }
    }

    private boolean checkIfNewSourceWasAdded() {
        return remoteUrl.get() != null && bankType != null;
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Optional<SourceObserver> getAddedSourceObserver() throws InvalidSourceConfigException {
        if (checkIfNewSourceWasAdded()) {

            String url = remoteUrl.get();
            if (!url.endsWith("/"))
                url += "/";

            SourceObserver sourceObserver = SourceObserverBuilderBuilder.with()
                    .withSourceType(SourceType.REST_API)
                    .withBankType(bankType)
                    .withDescription(url)
                    .build();

            return Optional.of(sourceObserver);
        }

        return Optional.empty();
    }
}
