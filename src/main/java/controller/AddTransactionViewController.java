package controller;

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

import java.io.File;
import java.io.IOException;

public class AddTransactionViewController {

    final FileChooser fileChooser = new FileChooser();

    @FXML
    public Button mbankButton;

    @FXML
    public Button santanderButton;

    @FXML
    public TextArea chosenFileArea;

    public void handleAddNewMBankStatement(ActionEvent actionEvent) {
        openFileChooser();

    }

    public void handleAddNewSantanderStatement(ActionEvent actionEvent) {
        openFileChooser();

    }

    public void openFileChooser() {
        fileChooser.setTitle("Choose statement");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            chosenFileArea.appendText(file.getAbsolutePath());
        } else {
            System.out.println("Invalid file");
        }
    }
}
