package controller.sources;

import controller.TransactionsManagerAppController;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.util.BankType;
import watcher.SourceObserver;
import watcher.SourceObserverFactory;
import watcher.SourceType;
import watcher.SourcesSupervisor;
import watcher.exceptions.InvalidSourceConfigException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class TransactionSourcesViewController {

    private Stage stage;

    private TransactionsManagerAppController appController;

    private final SourcesSupervisor sourcesSupervisor;

    private final ObservableList<SourceObserver> directorySourceObservers = FXCollections.observableArrayList();
    private final ObservableList<SourceObserver> remoteSourceObservers = FXCollections.observableArrayList();

    @Inject
    public TransactionSourcesViewController(SourcesSupervisor sourcesSupervisor) {
        this.sourcesSupervisor = sourcesSupervisor;
    }

    @FXML
    public Button addDirectoryButton;

    @FXML
    public Button deleteDirectoryButton;

    @FXML
    public Button addRemoteButton;

    @FXML
    public Button deleteRemoteButton;

    @FXML
    public TableView<SourceObserver> directoriesTable;

    @FXML
    public TableView<SourceObserver> remotesTable;

    @FXML
    public TableColumn<SourceObserver, String> directoryNameColumn;

    @FXML
    public TableColumn<SourceObserver, String> remoteUrlColumn;

    @FXML
    public TableColumn<SourceObserver, BankType> remoteBankColumn;

    @FXML
    public TableColumn<SourceObserver, BankType> directoryBankColumn;

    @FXML
    private void initialize() {
        directoriesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remotesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        directoriesTable.setItems(directorySourceObservers);
        remotesTable.setItems(remoteSourceObservers);

        directoryNameColumn.setCellValueFactory(directoryName -> directoryName.getValue().descriptionProperty());
        directoryBankColumn.setCellValueFactory(bankType -> bankType.getValue().bankTypeProperty());

        remoteUrlColumn.setCellValueFactory(remoteUrl -> remoteUrl.getValue().descriptionProperty());
        remoteBankColumn.setCellValueFactory(bankType -> bankType.getValue().bankTypeProperty());

        deleteDirectoryButton.disableProperty().bind(Bindings.size(directoriesTable.getSelectionModel().getSelectedItems()).isEqualTo(0));
        deleteRemoteButton.disableProperty().bind(Bindings.size(remotesTable.getSelectionModel().getSelectedItems()).isEqualTo(0));
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    public void handleAddDirectoryButton(ActionEvent actionEvent) {
        try {
            AddDirectorySourceWindowController addDirectoryController
                    = this.appController.showAddDirectorySourceWindow();

            String directoryPath = addDirectoryController.getSelectedDirectory().getAbsolutePath();
            BankType directoryBankType = addDirectoryController.getBankType();

            SourceObserver addedSourceObserver = SourceObserverFactory.initializeSourceObserver(
                    directoryBankType, directoryPath, SourceType.DIRECTORY
            );

            sourcesSupervisor.addSourceObserver(addedSourceObserver);
            directorySourceObservers.add(addedSourceObserver);

        } catch (InvalidSourceConfigException | IOException e) {
            e.printStackTrace(); //TODO error handling
        }
    }

    public void handleAddRemoteButton(ActionEvent actionEvent) {
        try {
            AddRemoteSourceWindowController addRemoteController
                    = this.appController.showAddRemoteSourceWindow();

            String remoteUrl = addRemoteController.getRemoteUrl();
            BankType remoteBankType = addRemoteController.getBankType();

            SourceObserver addedSourceObserver = SourceObserverFactory.initializeSourceObserver(
                    remoteBankType, remoteUrl, SourceType.REST_API
            );

            sourcesSupervisor.addSourceObserver(addedSourceObserver);
            remoteSourceObservers.add(addedSourceObserver);

        } catch (InvalidSourceConfigException | IOException e) {
            e.printStackTrace(); //TODO error handling
        }
    }

    public void handleDeleteDirectoryButton(ActionEvent actionEvent) {
        directoriesTable.getSelectionModel().getSelectedItems()
                .forEach(selectedDirectory -> {
                    sourcesSupervisor.removeSourceObserver(selectedDirectory);
                    directorySourceObservers.remove(selectedDirectory);
                });
    }

    public void handleDeleteRemoteButton(ActionEvent actionEvent) {
        remotesTable.getSelectionModel().getSelectedItems()
                .forEach(selectedDirectory -> {
                    sourcesSupervisor.removeSourceObserver(selectedDirectory);
                    remoteSourceObservers.remove(selectedDirectory);
                });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
