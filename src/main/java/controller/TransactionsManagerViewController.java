package controller;

import controller.util.ContextMenuRowFactory;
import importer.Importer;
import importer.exceptions.ParserException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.scene.control.*;
import model.Account;
import model.TransactionsSupervisor;
import model.util.BankType;
import model.util.DocumentType;
import model.util.ImportSession;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import model.BankTransaction;
import model.util.TransactionCategory;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionsManagerViewController {

    private final ObservableList<BankTransaction> bankTransactions;
    private final TransactionsSupervisor transactionsSupervisor;
    private final Importer importer;
    private final Account account;

    private TransactionsManagerAppController appController;


    @Inject
    public TransactionsManagerViewController(TransactionsSupervisor transactionsSupervisor,
                                             Importer importer, Account account) {
        this.transactionsSupervisor = transactionsSupervisor;
        this.importer = importer;
        this.account = account;

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
    public TextField balanceTextField;

    @FXML
    public Button categoryChangeButton;

    @FXML
    public ComboBox<TransactionCategory> categoryComboBox;

    @FXML
    public ContextMenu contextMenu;

    @FXML
    private void initialize() {
        transactionsTable.setItems(bankTransactions);
        updateCategoryComboBox();

        transactionsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        balanceTextField.textProperty().bind(account
                .balanceProperty().asString("Transactions Balance: %.2f"));

        dateColumn.setCellValueFactory(dataValue -> dataValue.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(dataValue -> dataValue.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(dataValue -> dataValue.getValue().amountProperty());
        categoryColumn.setCellValueFactory(dataValue -> dataValue.getValue().categoryProperty());

        editButton.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        categoryChangeButton.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isEqualTo(0));
        categoryComboBox.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isEqualTo(0));

        contextMenu.setStyle("-fx-min-width: 120.0; -fx-min-height: 40.0;");
        transactionsTable.setRowFactory(new ContextMenuRowFactory<>(contextMenu));
    }

    private void updateCategoryComboBox() {
        categoryComboBox.getItems().addAll(TransactionCategory.values());
        categoryComboBox.getSelectionModel().select(TransactionCategory.UNCATEGORIZED);
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

                handleImport(selectedBank, selectedDocType, filePath);
            }
        } catch (IOException e) {
            System.out.println("Failed to load window");
            e.printStackTrace();
        }
    }

    private void handleImport(BankType selectedBank, DocumentType selectedDocType, String uri) {
        ImportSession importSession = transactionsSupervisor.startImportSession();

        try {
            importer.importBankStatement(selectedBank, selectedDocType, uri)
                    .subscribeOn(Schedulers.io())
                    .filter(transaction -> transactionsSupervisor.tryToAddTransaction(importSession, transaction))
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(account::addTransaction,
                          err -> handleImportError(importSession, err),
                          () -> handleImportComplete(importSession, uri));

        } catch (IOException e) {
            this.appController.showErrorWindow("Failed to read statement from " + uri, e.getMessage());
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
}
