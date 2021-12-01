package controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.BankType;

import java.io.File;
import java.io.IOException;

public class AddStatementViewController {

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
        fileChooser.setInitialDirectory(new File("src/test/resources")); // TODO
        file.setValue(fileChooser.showOpenDialog(stage));

        if(file.get() != null) {
            chosenFileArea.appendText(file.get().getAbsolutePath());
        } else {
            System.out.println("Invalid file");
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
