package controller.sources;

import controller.TransactionsManagerAppController;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.util.BankType;

import java.io.IOException;

public class TransactionSourcesViewController {

    private Stage stage;

    private TransactionsManagerAppController appController;

    @FXML
    public Button addDirectoryButton;

    @FXML
    public Button deleteDirectoryButton;

    @FXML
    public Button addRemoteButton;

    @FXML
    public Button deleteRemoteButton;

    @FXML
    public TableView<String> directoriesTable;

    @FXML
    public TableView<String> remotesTable;

    @FXML
    public TableColumn<String, String> directoryNameColumn;

    @FXML
    public TableColumn<String, String> remoteUrlColumn;

    @FXML
    public TableColumn<String, BankType> remoteBankColumn;

    @FXML
    public TableColumn<String, BankType> directoryBankColumn;

    @FXML
    private void initialize() {
        directoriesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remotesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        deleteDirectoryButton.disableProperty().bind(Bindings.size(directoriesTable.getSelectionModel().getSelectedItems()).isEqualTo(0));
        deleteRemoteButton.disableProperty().bind(Bindings.size(remotesTable.getSelectionModel().getSelectedItems()).isEqualTo(0));
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    public void handleDeleteDirectoryButton(ActionEvent actionEvent) {

    }

    public void handleAddDirectoryButton(ActionEvent actionEvent) {
        try {
            this.appController.showAddDirectorySourceWindow();
        } catch (IOException e) {
            e.printStackTrace(); //TODO error handling
        }
    }

    public void handleAddRemoteButton(ActionEvent actionEvent) {
        try {
            this.appController.showAddRemoteSourceWindow();
        } catch (IOException e) {
            e.printStackTrace(); //TODO error handling
        }
    }

    public void handleDeleteRemoteButton(ActionEvent actionEvent) {

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
