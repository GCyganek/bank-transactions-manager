package controller.sources;

import com.google.inject.Inject;
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
import watcher.InvalidSourceConfigException;
import watcher.SourceObserverFactory;
import watcher.SourceType;
import watcher.SourcesSupervisor;

import java.io.File;
import java.io.IOException;

public class AddDirectorySourceWindowController {

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

    private final SourcesSupervisor sourcesSupervisor;

    @Inject
    public AddDirectorySourceWindowController(SourcesSupervisor sourcesSupervisor) {
        this.sourcesSupervisor = sourcesSupervisor;
    }

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
        try {
            sourcesSupervisor.addSourceObserver(
                    SourceObserverFactory.initializeSourceObserver(
                            bankType, selectedDirectory.getValue().getAbsolutePath(), SourceType.DIRECTORY)
            );
        } catch (InvalidSourceConfigException | IOException e) {
            e.printStackTrace(); //TODO
        }
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

    private void setBankType(BankType bankType) { this.bankType = bankType; }

    public File getSelectedDirectory() { return this.selectedDirectory.get(); }

    public BankType getBankType() { return this.bankType; }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
