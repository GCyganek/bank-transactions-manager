package controller.sources;

import com.google.inject.Singleton;
import controller.TransactionsManagerAppController;
import controller.sources.util.SourceTable;
import javafx.application.Platform;
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
import watcher.SourceType;
import watcher.SourcesSupervisor;
import watcher.exceptions.DuplicateSourceException;
import watcher.exceptions.InvalidSourceConfigException;

import javax.inject.Inject;
import java.util.*;

@Singleton
public class TransactionSourcesViewController {

    private Stage stage;

    private TransactionsManagerAppController appController;

    private final SourcesSupervisor sourcesSupervisor;

    private final ObservableList<SourceObserver> sourceObservers = FXCollections.observableArrayList();
    @Inject
    public TransactionSourcesViewController(SourcesSupervisor sourcesSupervisor) {
        this.sourcesSupervisor = sourcesSupervisor;
        this.sourceTables = new ArrayList<>();
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
    public Button reactivateDirectoryButton;

    @FXML
    public Button reactivateRemoteButton;

    @FXML
    public TableView<SourceObserver> directoriesTable;

    @FXML
    public TableView<SourceObserver> remotesTable;

    @FXML
    public TableColumn<SourceObserver, Boolean> remoteActiveColumn;

    @FXML
    public TableColumn<SourceObserver, Boolean> directoryActiveColumn;

    @FXML
    public TableColumn<SourceObserver, String> directoryNameColumn;

    @FXML
    public TableColumn<SourceObserver, String> remoteUrlColumn;

    @FXML
    public TableColumn<SourceObserver, BankType> remoteBankColumn;

    @FXML
    public TableColumn<SourceObserver, BankType> directoryBankColumn;

    private final ArrayList<SourceTable> sourceTables;

    @FXML
    private void initialize() {
        Arrays.stream(SourceType.values()).forEach(sourceType -> {
            SourceTable sourceTable = switch (sourceType) {
                case REST_API ->
                        new SourceTable(remotesTable, remoteUrlColumn, remoteBankColumn,
                                remoteActiveColumn, deleteRemoteButton, reactivateRemoteButton);
                case DIRECTORY ->
                        new SourceTable(directoriesTable, directoryNameColumn, directoryBankColumn,
                                directoryActiveColumn, deleteDirectoryButton, reactivateDirectoryButton);
            };
            sourceTables.add(sourceType.ordinal(), sourceTable);
            setupSourceTable(sourceTables.get(sourceType.ordinal()), sourceType);
        });
    }

    private void setupSourceTable(SourceTable sourceTable, SourceType sourceType) {
        sourceTable.tableView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sourceTable.tableView()
                .setItems(sourceObservers.filtered(sourceObserver -> sourceObserver.getSourceType() == sourceType));

        sourceTable.descriptionColumn().setCellValueFactory(description -> description.getValue().descriptionProperty());
        sourceTable.bankTypeColumn().setCellValueFactory(bankType -> bankType.getValue().bankTypeProperty());
        sourceTable.activeColumn().setCellValueFactory(active -> active.getValue().activeProperty());

        sourceTable.deleteButton().disableProperty()
                .bind(Bindings.size(sourceTable.tableView().getSelectionModel().getSelectedItems()).isEqualTo(0));
        sourceTable.reactivateButton().disableProperty()
                .bind(Bindings.size(sourceTable.tableView().getSelectionModel().getSelectedItems()).isEqualTo(0));
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    private void handleAddSourceButton(SourceAdditionWindowController controller) throws DuplicateSourceException, InvalidSourceConfigException {
        Optional<SourceObserver> sourceObserverOptional = controller.getAddedSourceObserver();

        if (sourceObserverOptional.isPresent()) {
            SourceObserver sourceObserver = sourceObserverOptional.get();
            String description = sourceObserver.descriptionProperty().get();

            if (checkDuplicate(description, sourceObservers))
                throw new DuplicateSourceException(description);

            sourcesSupervisor.addSourceObserver(sourceObserver);
            sourceObservers.add(sourceObserver);

            setupSourceFailureObserver(sourceObserver);
        }

    }

    private void setupSourceFailureObserver(SourceObserver sourceObserver) {
        sourceObserver
                .getSourceFailedObservable()
                .subscribe(err -> {
                    String sourceDescription = sourceObserver.descriptionProperty().get();
                    Platform.runLater(
                            () -> {
                                this.appController.showErrorWindow(
                                        "Stopped listening to the source: " + sourceDescription,
                                        err.getMessage()
                                );
                            }
                    );
                    sourceObserver.setActive(false);
                });
    }

    public void handleAddDirectoryButton(ActionEvent actionEvent) {
        this.appController.showAddDirectorySourceWindow().ifPresent(addDirectoryController -> {
            try {
                handleAddSourceButton(addDirectoryController);
            } catch (InvalidSourceConfigException | DuplicateSourceException e) {
                this.appController.showErrorWindow("Failed to add directory source", e.getMessage());
            }
        });
    }

    public void handleAddRemoteButton(ActionEvent actionEvent) {
        this.appController.showAddRemoteSourceWindow().ifPresent(addRemoteController -> {
            try {
                handleAddSourceButton(addRemoteController);
            } catch (DuplicateSourceException | InvalidSourceConfigException e) {
                this.appController.showErrorWindow("Failed to add remote source", e.getMessage());
            }
        });
    }

    private void removeSource(SourceType sourceType) {
        SourceTable sourceTable = sourceTables.get(sourceType.ordinal());
        List<SourceObserver> selectedItems = sourceTable.tableView().getSelectionModel().getSelectedItems();
        selectedItems.forEach(x -> System.out.println(x.descriptionProperty().get()));
        selectedItems.forEach(sourcesSupervisor::removeSourceObserver);
        sourceObservers.removeAll(selectedItems);
    }

    private void reactivateSources(SourceType sourceType) {
        SourceTable sourceTable = sourceTables.get(sourceType.ordinal());
        List<SourceObserver> selectedItems = sourceTable.tableView().getSelectionModel().getSelectedItems();
        selectedItems.forEach(sourceObserver -> {
            System.out.println(sourceObserver.descriptionProperty().get() + " " + sourceObserver.activeProperty().get());
            if (sourceObserver.activeProperty().get()) return;
            sourceObserver.setActive(true);
        });
    }

    public void handleDeleteDirectoryButton(ActionEvent actionEvent) {
        removeSource(SourceType.DIRECTORY);
    }

    public void handleDeleteRemoteButton(ActionEvent actionEvent) {
        removeSource(SourceType.REST_API);
    }

    public void handleReactivateDirectoryButton(ActionEvent actionEvent) {
        reactivateSources(SourceType.DIRECTORY);
    }

    public void handleReactivateRemoteButton(ActionEvent actionEvent) {
        reactivateSources(SourceType.REST_API);
    }

    private boolean checkDuplicate(String source, List<SourceObserver> sourceObservers) {
        return sourceObservers.stream().anyMatch(sourceObserver -> sourceObserver.descriptionProperty().get().equals(source));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public ObservableList<SourceObserver> getSourceObservers() {
        return sourceObservers;
    }
}
