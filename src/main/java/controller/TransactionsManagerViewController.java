package controller;

import controller.util.ContextMenuRowFactory;
import importer.Importer;
import importer.exceptions.ParserException;
import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Account;
import model.BankTransaction;
import model.TransactionsSupervisor;
import model.util.BankType;
import model.util.DocumentType;
import model.util.ImportSession;
import model.util.TransactionCategory;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import settings.SettingsConfigurator;
import watcher.SourceUpdate;
import watcher.SourcesRefresher;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TransactionsManagerViewController {
    private static final long SOURCES_REFRESH_PERIOD = 5; // TODO make this configurable from gui and save in config file
    
    private final ObservableList<BankTransaction> bankTransactions;
    private final TransactionsSupervisor transactionsSupervisor;
    private final Importer importer;
    private final Account account;
    private final SourcesRefresher sourcesRefresher;
    private final SettingsConfigurator settingsConfigurator;

    private TransactionsManagerAppController appController;

    @Inject
    public TransactionsManagerViewController(TransactionsSupervisor transactionsSupervisor,
                                             Importer importer, Account account,
                                             SettingsConfigurator settingsConfigurator,
                                             SourcesRefresher sourcesRefresher) {
        this.transactionsSupervisor = transactionsSupervisor;
        this.settingsConfigurator = settingsConfigurator;
        this.importer = importer;
        this.account = account;
        this.sourcesRefresher = sourcesRefresher;

        this.bankTransactions = account.getTransactionObservableList();
    }

    @FXML
    public TableView<BankTransaction> transactionsTable;

    @FXML
    public TableColumn<BankTransaction, LocalDate> dateColumn;

    @FXML
    public TableColumn<BankTransaction, String> descriptionColumn;

    @FXML
    public TableColumn<BankTransaction, BigDecimal> amountColumn;

    @FXML
    public TableColumn<BankTransaction, TransactionCategory> categoryColumn;

    @FXML
    public Button addButton;

    @FXML
    public Button editButton;

    @FXML
    public Button statsButton;

    @FXML
    public Button importFromSourcesButton;

    @FXML
    public Button manageSourcesButton;

    @FXML
    public TextField balanceTextField;

    @FXML
    public Button categoryChangeButton;

    @FXML
    public ComboBox<TransactionCategory> categoryComboBox;

    @FXML
    public ContextMenu contextMenu;

    @FXML
    public Label updatesCountLabel;

    @FXML
    public CheckBox autoImportCheckbox;

    @FXML
    private void initialize() {
        transactionsTable.setItems(bankTransactions);
        updateCategoryComboBox();

        transactionsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        balanceTextField.textProperty().bind(account
                .balanceProperty().asString("Transactions Balance: %.2f"));

        dateColumn.setCellValueFactory(dataValue -> dataValue.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(descriptionValue -> descriptionValue.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(amountValue -> amountValue.getValue().amountProperty());
        categoryColumn.setCellValueFactory(categoryValue -> categoryValue.getValue().categoryProperty());

        editButton.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        categoryChangeButton.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isEqualTo(0));
        categoryComboBox.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isEqualTo(0));

        contextMenu.setStyle("-fx-min-width: 120.0; -fx-min-height: 40.0;");
        transactionsTable.setRowFactory(new ContextMenuRowFactory<>(contextMenu));

        setupAutoImportCheckbox();
        listenForNewUpdates();
    }

    private void updateCategoryComboBox() {
        categoryComboBox.getItems().addAll(TransactionCategory.values());
        categoryComboBox.getSelectionModel().select(TransactionCategory.UNCATEGORIZED);
    }

    private void listenForNewUpdates() {
        // TODO check in settings if this should be started
        sourcesRefresher.startPeriodicalUpdateChecks(SOURCES_REFRESH_PERIOD, TimeUnit.SECONDS);

        IntegerBinding availableUpdatesCount = sourcesRefresher.getAvailableUpdatesCount();

        updatesCountLabel.textProperty().bind(availableUpdatesCount.asString());

        sourcesRefresher
                .getUpdateFetchedObservable()
                .subscribe(sourceUpdate -> {
                    if (autoImportCheckbox.isSelected())
                        importFromSources(sourcesRefresher.getCachedSourceUpdates());
                });
    }

    private void setupAutoImportCheckbox() {
        autoImportCheckbox.setSelected(settingsConfigurator.getAutoImportStatus());

        autoImportCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingsConfigurator.setAutoImportStatus(newValue);
            if (newValue) {
                importFromSources(sourcesRefresher.getCachedSourceUpdates());
            }
        });
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    public void handleEditBankTransaction(ActionEvent actionEvent) {
        BankTransaction bankTransaction = transactionsTable.getSelectionModel().getSelectedItem();
        BigDecimal amountBeforeEdit = bankTransaction.getAmount();

        if (bankTransaction != null) {
            appController.showEditTransactionWindow(bankTransaction).ifPresent(amountAfterEdit -> {
                if (amountAfterEdit.compareTo(amountBeforeEdit) != 0) {
                    account.addToBalance(amountAfterEdit.subtract(amountBeforeEdit));
                }
            });
        }
    }

    public void handleShowStatistics(ActionEvent actionEvent) {
        appController.showStatisticsView();
    }

    public void handleAddNewBankStatement(ActionEvent actionEvent) {
        try {
            AddStatementViewController addStatementViewController = appController.showAddStatementView();

            if (addStatementViewController.checkIfFileAvailable()) {
                BankType selectedBank = addStatementViewController.getBankType();
                DocumentType selectedDocType = DocumentType.CSV;
                String filePath = addStatementViewController.getFile().getAbsolutePath();

                handleImport(new LocalFSLoader(filePath, selectedBank, selectedDocType));
            }
        } catch (IOException e) {
            System.out.println("Failed to load window");
            e.printStackTrace();
        }
    }

    private void handleImport(Loader loader) {
        ImportSession importSession = transactionsSupervisor.startImportSession();
        String loaderDescritpion = loader.getDescription();

        try {
            importer.importBankStatement(loader)
                    .subscribeOn(Schedulers.io())
                    .filter(transaction -> transactionsSupervisor.tryToAddTransaction(importSession, transaction))
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(account::addTransaction,
                          err -> handleImportError(importSession, err),
                          () -> handleImportComplete(importSession, loaderDescritpion));

        } catch (IOException e) {
            this.appController.showErrorWindow("Failed to read statement from " + loaderDescritpion, e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleImportError(ImportSession importSession, Throwable err) {
        String reason = "";
        if (err instanceof ParserException e) {
            reason = e.getReason();
        }
        this.appController.showErrorWindow(err.getMessage(), reason);
        this.transactionsSupervisor.reverseImport(importSession);
    }

    private void handleImportComplete(ImportSession importSession, String uri) {
        int filteredCount = transactionsSupervisor.completeImport(importSession);
        if (filteredCount > 0) {
            this.appController
                    .showErrorWindow("Failed to import some transactions from: " + uri,
                            "Duplicated Transactions: " + filteredCount);
        }
    }

    public void handleCategoryChangeButton(ActionEvent actionEvent) {
        TransactionCategory selectedTransactionCategory = categoryComboBox.getValue();
        List<BankTransaction> selectedBankTransactions = transactionsTable.getSelectionModel().getSelectedItems();
        updateSelectedBankTransactionsCategory(selectedBankTransactions, selectedTransactionCategory);
    }

    private void updateSelectedBankTransactionsCategory(List<BankTransaction> bankTransactions,
                                                        TransactionCategory transactionCategory)
    {
        bankTransactions.forEach(bankTransaction -> {
            if (transactionCategory.equals(bankTransaction.getCategory())) return;
            BankTransaction editedTransaction = getEditedTransaction(bankTransaction, transactionCategory);

            if (!transactionsSupervisor.updateTransaction(bankTransaction, editedTransaction)) {
                appController.showErrorWindow("Failed to update transaction.", "Transaction with these fields already exits");
            }
        });
    }

    private BankTransaction getEditedTransaction(BankTransaction bankTransaction,
                                                 TransactionCategory transactionCategory)
    {
        BankTransaction editedTransaction = bankTransaction.shallowCopy();

        editedTransaction.setCategory(transactionCategory);

        return editedTransaction;
    }

    public void handleImportFromSourcesButton(ActionEvent actionEvent) {
        importFromSourcesButton.disableProperty().setValue(true);

        importFromSources(sourcesRefresher.getUpdates());
    }

    private void importFromSources(Observable<SourceUpdate> sourceUpdates) {
        sourceUpdates
                .subscribeOn(Schedulers.io())
                .doOnNext(sourceUpdate -> sourceUpdate.getSourceObserver().changeImported(sourceUpdate))
                .flatMap(sourceUpdate -> sourceUpdate.getUpdateDataLoader().toObservable())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(this::handleImport,
                        err -> this.appController.showErrorWindow("Failed to get update", err.getMessage()),
                        () -> {
                            settingsConfigurator.updateSourcesConfig(sourcesRefresher.getSourceObservers());
                            importFromSourcesButton.disableProperty().setValue(false);
                        });
    }

    public void handleManageSourcesButton(ActionEvent actionEvent) {
        this.appController.showTransactionSourcesWindow();
    }
}
