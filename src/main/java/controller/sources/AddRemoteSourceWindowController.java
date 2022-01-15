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
import watcher.InvalidSourceConfigException;
import watcher.SourceObserverFactory;
import watcher.SourceType;
import watcher.SourcesSupervisor;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class AddRemoteSourceWindowController {
    private Stage stage;

    private BankType bankType;

    private TransactionsManagerAppController appController;

    private final SourcesSupervisor sourcesSupervisor;

    private final SimpleStringProperty remoteUrl = new SimpleStringProperty();

    @Inject
    public AddRemoteSourceWindowController(SourcesSupervisor sourcesSupervisor) {
        this.sourcesSupervisor = sourcesSupervisor;
    }

    @FXML
    public ChoiceBox<BankType> remoteBankChoiceBox;

    @FXML
    public Button addRemoteSourceButton;

    @FXML
    public TextField remoteSourceTextField;

    @FXML
    private void initialize() {
        remoteBankChoiceBox.getItems().addAll(BankType.values());
        addRemoteSourceButton.disableProperty().bind(
                Bindings.length(remoteSourceTextField.textProperty()).isEqualTo(0)
                        .or(Bindings.isNull(remoteBankChoiceBox.valueProperty()))
        );
    }

    public void handleAddRemoteSourceButton(ActionEvent actionEvent) {
        if (!validateUrl(remoteSourceTextField.getText())) return;
        remoteUrl.setValue(remoteSourceTextField.getText());
        bankType = remoteBankChoiceBox.getValue();
        try {
            sourcesSupervisor.addSourceObserver(
                    SourceObserverFactory.initializeSourceObserver(
                            bankType, remoteUrl.getValue(), SourceType.REST_API)
            );
        } catch (InvalidSourceConfigException | IOException e) {
            e.printStackTrace(); //TODO
        }
        stage.close();
    }

    public BankType getBankType() { return this.bankType; }

    public String getRemoteUrl() { return this.remoteUrl.get(); }

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

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
