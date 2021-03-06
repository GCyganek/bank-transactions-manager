package controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.util.BankType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class AddStatementViewController {
    private static final Logger LOGGER = LogManager.getLogger(AddStatementViewController.class);

    final FileChooser fileChooser = new FileChooser();

    private Stage stage;

    private BankType bankType;

    private final ObjectProperty<File> file = new SimpleObjectProperty<>();

    @FXML
    public Button mbankButton;

    @FXML
    public Button santanderButton;

    @FXML
    public Button addButton;

    @FXML
    public TextArea chosenFileArea;

    @FXML
    private void initialize() {
        addButton.disableProperty().bind(Bindings.isNull(file));
    }

    public void handleAddNewMBankStatement(ActionEvent actionEvent) {
        bankType = BankType.MBANK;
        openFileChooser();
    }

    public void handleAddNewSantanderStatement(ActionEvent actionEvent) {
        bankType = BankType.SANTANDER;
        openFileChooser();
    }

    public void handleAddButton(ActionEvent actionEvent) {
        stage.close();
    }

    public void openFileChooser() {
        chosenFileArea.clear();
        fileChooser.setTitle("Choose statement");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialDirectory(new File("src/test/resources"));
        file.setValue(fileChooser.showOpenDialog(stage));

        if(file.get() != null) {
            chosenFileArea.appendText(file.get().getAbsolutePath());
        } else {
            LOGGER.info("Invalid file");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public BankType getBankType() {
        return bankType;
    }

    public File getFile() {
        return file.get();
    }

    public boolean checkIfFileAvailable() {
        return file.get() != null;
    }
}
