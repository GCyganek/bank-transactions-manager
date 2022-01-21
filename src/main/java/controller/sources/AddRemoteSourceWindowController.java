package controller.sources;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import model.util.BankType;
import model.util.SourceType;
import watcher.SourceObserver;
import watcher.exceptions.InvalidSourceConfigException;

import java.util.Optional;

public class AddRemoteSourceWindowController extends AbstractSourceAdditionWindowController {

    private BankType bankType;

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

    private boolean checkIfNewSourceWasAdded() {
        return remoteUrl.get() != null && bankType != null;
    }

    @Override
    public Optional<SourceObserver> getAddedSourceObserver() throws InvalidSourceConfigException {
        if (checkIfNewSourceWasAdded()) {
            String url = remoteUrl.get();
            if (!url.endsWith("/"))
                url += "/";

            return buildAddedSourceObserver(SourceType.REST_API, bankType, url);
        }

        return Optional.empty();
    }
}
