package controller.sources;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.util.BankType;
import watcher.SourceObserver;
import watcher.SourceType;
import watcher.builder.SourceObserverBuilder;
import watcher.exceptions.InvalidSourceConfigException;

import java.io.File;
import java.util.Optional;

public class AddDirectorySourceWindowController implements SourceAdditionWindowController {

    private Stage stage;

    private BankType bankType;

    private final ObjectProperty<File> selectedDirectory = new SimpleObjectProperty<>();

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    public TextArea directoryTextArea;

    @FXML
    public Button chooseDirectoryButton;

    @FXML
    public ChoiceBox<BankType> directoryBankChoiceBox;

    @FXML
    public Button addDirectoryButton;

    @FXML
    private void initialize() {
        directoryBankChoiceBox.getItems().addAll(BankType.values());
        addDirectoryButton.disableProperty().bind(
                Bindings.isNull(selectedDirectory)
                        .or(Bindings.isNull(directoryBankChoiceBox.valueProperty()))
        );
    }

    public void handleAddDirectoryButton(ActionEvent actionEvent) {
        bankType = directoryBankChoiceBox.getValue();
        stage.close();
    }

    private void getDirectoryFromDirectoryChooser() {
        directoryChooser.setTitle("Choose directory");
        var oldSelectedDirectory = selectedDirectory.get();
        selectedDirectory.setValue(directoryChooser.showDialog(stage));

        if (selectedDirectory.get() != null) {
            directoryTextArea.clear();
            directoryTextArea.appendText(selectedDirectory.get().getAbsolutePath());
        } else {
            selectedDirectory.setValue(oldSelectedDirectory);
        }
    }

    public void handleChooseDirectoryButton(ActionEvent actionEvent) {
        getDirectoryFromDirectoryChooser();
    }

    private boolean checkIfNewSourceWasAdded() {
        return selectedDirectory.get() != null && bankType != null;
    }

    private File getSelectedDirectory() { return this.selectedDirectory.get(); }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Optional<SourceObserver> getAddedSourceObserver() throws InvalidSourceConfigException {
        if (checkIfNewSourceWasAdded()) {
            SourceObserver sourceObserver = SourceObserverBuilder.with()
                    .withSourceType(SourceType.DIRECTORY)
                    .withBankType(bankType)
                    .withDescription(getSelectedDirectory().getAbsolutePath())
                    .build();

            return Optional.of(sourceObserver);
        }

        return Optional.empty();
    }
}
