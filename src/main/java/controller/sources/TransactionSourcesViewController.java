package controller.sources;

import com.google.inject.Singleton;
import controller.TransactionsManagerAppController;
import controller.sources.util.SourceTable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.util.BankType;
import settings.SettingsConfigurator;
import watcher.SourceObserver;
import model.util.SourceType;
import watcher.SourcesRefresher;
import watcher.exceptions.DuplicateSourceException;
import watcher.exceptions.InvalidSourceConfigException;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class TransactionSourcesViewController {

    private Stage stage;

    private TransactionsManagerAppController appController;

    private final SourcesRefresher sourcesRefresher;

    private final SettingsConfigurator settingsConfigurator;

    @Inject
    public TransactionSourcesViewController(SourcesRefresher sourcesRefresher,
                                            SettingsConfigurator settingsConfigurator)
    {
        this.sourcesRefresher = sourcesRefresher;
        this.settingsConfigurator = settingsConfigurator;
        this.sourceTables = new ArrayList<>();

        setupSettings();
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
    public Button deactivateDirectoryButton;

    @FXML
    public Button reactivateRemoteButton;

    @FXML
    public Button deactivateRemoteButton;

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
                                remoteActiveColumn, deleteRemoteButton, reactivateRemoteButton, deactivateRemoteButton);
                case DIRECTORY ->
                        new SourceTable(directoriesTable, directoryNameColumn, directoryBankColumn,
                                directoryActiveColumn, deleteDirectoryButton, reactivateDirectoryButton, deactivateDirectoryButton);
            };
            sourceTables.add(sourceType.ordinal(), sourceTable);
            setupSourceTable(sourceTables.get(sourceType.ordinal()), sourceType);
        });
    }

    private void setupSettings() {
        for (var sourceObserver: settingsConfigurator.getStoredSources()) {
            try {
                addSourceObserver(sourceObserver);
            } catch (DuplicateSourceException e) {
                // just ignore it
                e.printStackTrace();
            }
        }

        settingsConfigurator.listenForSourcesExistenceChange(sourcesRefresher.getSourceObservers());
    }

    private void setupSourceTable(SourceTable sourceTable, SourceType sourceType) {
        ObservableList<SourceObserver> sourceObservers = sourcesRefresher.getSourceObservers();

        sourceTable.tableView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sourceTable.tableView()
                .setItems(sourceObservers.filtered(sourceObserver -> sourceObserver.getSourceType() == sourceType));

        sourceTable.descriptionColumn().setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getDescription()));
        sourceTable.bankTypeColumn().setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getBankType()));
        sourceTable.activeColumn().setCellValueFactory(row -> row.getValue().activeProperty());

        sourceTable.deleteButton().disableProperty()
                .bind(selectionEmpty(sourceTable));
        sourceTable.reactivateButton().disableProperty()
                .bind(selectionEmpty(sourceTable));
        sourceTable.deactivateButton().disableProperty()
                .bind(selectionEmpty(sourceTable));
    }

    private BooleanBinding selectionEmpty(SourceTable sourceTable) {
       return Bindings.size(sourceTable.tableView().getSelectionModel().getSelectedItems()).isEqualTo(0);
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    private void handleAddSourceButton(SourceAdditionWindowController controller, String errorMsg) {
        try {
            Optional<SourceObserver> sourceObserverOptional = controller.getAddedSourceObserver();

            if (sourceObserverOptional.isPresent()) {
                addSourceObserver(sourceObserverOptional.get());
            }
        } catch (InvalidSourceConfigException | DuplicateSourceException e) {
            this.appController.showErrorWindow(errorMsg, e.getMessage());
        }
    }

    private void addSourceObserver(SourceObserver sourceObserver) throws DuplicateSourceException {
        sourcesRefresher.addSourceObserver(sourceObserver);
        setupSourceFailureObserver(sourceObserver);
    }

    private void setupSourceFailureObserver(SourceObserver sourceObserver) {
        sourceObserver
                .getSourceFailedObservable()
                .subscribe(err -> {
                    String sourceDescription = sourceObserver.getDescription();
                    Platform.runLater(
                            () -> {
                                this.appController.showErrorWindow(
                                        "Stopped listening to the source: " + sourceDescription,
                                        err.getMessage()
                                );
                                sourcesRefresher.deactivateSource(sourceObserver);
                            }
                    );
                });
    }

    private void removeSources(SourceType sourceType) {
        SourceTable sourceTable = sourceTables.get(sourceType.ordinal());
        List<SourceObserver> selectedItems = List.copyOf(sourceTable.tableView().getSelectionModel().getSelectedItems());
        selectedItems.forEach(sourcesRefresher::removeSourceObserver);
    }

    private void reactivateSources(SourceType sourceType) {
        SourceTable sourceTable = sourceTables.get(sourceType.ordinal());
        List<SourceObserver> selectedItems = sourceTable.tableView().getSelectionModel().getSelectedItems();
        selectedItems.forEach(sourcesRefresher::reactivateSource);
    }

    private void deactivateSources(SourceType sourceType) {
        SourceTable sourceTable = sourceTables.get(sourceType.ordinal());
        List<SourceObserver> selectedItems = sourceTable.tableView().getSelectionModel().getSelectedItems();
        selectedItems.forEach(sourcesRefresher::deactivateSource);
    }

    public void handleAddDirectoryButton(ActionEvent actionEvent) {
        this.appController.showAddDirectorySourceWindow().ifPresent(addDirectoryController -> {
            handleAddSourceButton(addDirectoryController, "Failed to add directory source");
        });
    }

    public void handleAddRemoteButton(ActionEvent actionEvent) {
        this.appController.showAddRemoteSourceWindow().ifPresent(addRemoteController -> {
                handleAddSourceButton(addRemoteController, "Failed to add remote source");
        });
    }

    public void handleDeleteDirectoryButton(ActionEvent actionEvent) {
        removeSources(SourceType.DIRECTORY);
    }

    public void handleDeleteRemoteButton(ActionEvent actionEvent) {
        removeSources(SourceType.REST_API);
    }

    public void handleReactivateDirectoryButton(ActionEvent actionEvent) {
        reactivateSources(SourceType.DIRECTORY);
    }

    public void handleDeactivateDirectoryButton(ActionEvent actionEvent) {
        deactivateSources(SourceType.DIRECTORY);
    }

    public void handleReactivateRemoteButton(ActionEvent actionEvent) {
        reactivateSources(SourceType.REST_API);
    }

    public void handleDeactivateRemoteButton(ActionEvent actionEvent) {
        deactivateSources(SourceType.REST_API);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
