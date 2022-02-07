package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ErrorViewController {

    private Stage stage;

    @FXML
    public Button closeErrBtn;

    @FXML
    public Label errorLabel;

    @FXML
    public Label errorReasonLabel;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setErrorMessage(String errorMessage, String reason) {
        this.errorLabel.setText(errorMessage);
        this.errorReasonLabel.setText(reason);
    }


    public void hideError() {
        stage.close();
    }
}
