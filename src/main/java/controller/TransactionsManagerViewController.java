package controller;

import importer.Importer;
import importer.exceptions.ParserException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import model.TransactionsManager;
import model.util.BankType;
import model.util.DocumentType;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.BankTransaction;
import model.util.TransactionCategory;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionsManagerViewController {

    private final ObservableList<BankTransaction> bankTransactions;
    private final TransactionsManager transactionsManager;
    private final Importer importer;

    private TransactionsManagerAppController appController;


    @Inject
    public TransactionsManagerViewController(TransactionsManager transactionsManager, Importer importer) {
        this.transactionsManager = transactionsManager;
        this.importer = importer;

        this.bankTransactions = this.transactionsManager.fetchDataFromDatabase();
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
    public TextField balanceTextField;

    @FXML
    private void initialize() {
        transactionsTable.setItems(bankTransactions);

        balanceTextField.textProperty().bind(transactionsManager
                .balanceProperty().asString("Transactions Balance: %.00f"));

        dateColumn.setCellValueFactory(dataValue -> dataValue.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(dataValue -> dataValue.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(dataValue -> dataValue.getValue().amountProperty());
        categoryColumn.setCellValueFactory(dataValue -> dataValue.getValue().categoryProperty());

        editButton.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
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
                    transactionsManager.addToBalance(amountAfterEdit.subtract(amountBeforeEdit));
                }
            });
        }
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
        int sessionId = transactionsManager.startImportSession();

        try {
            importer.importBankStatement(selectedBank, selectedDocType, uri)
                    .subscribeOn(Schedulers.io())
                    .filter(transaction   -> transactionsManager.isValid(sessionId, transaction))
                    .doOnNext(transaction -> transaction.getBankStatement().addBankTransaction(transaction))
                    .doOnNext(transaction -> transactionsManager.addTransaction(sessionId, transaction))
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(transactionsManager::addToView,
                          err -> handleImportError(err, sessionId),
                          () -> handleImportComplete(sessionId, uri));

        } catch (IOException e) {
            this.appController.showErrorWindow("Failed to read statement from " + uri, e.getMessage());
            e.printStackTrace();
            transactionsManager.clearSession(sessionId);
        }
    }

    private void handleImportError(Throwable err, int sessionId) {
        String reason = "";
        if (err instanceof ParserException e) {
            reason = e.getReason();
        }
        this.appController.showErrorWindow(err.getMessage(), reason);
        this.transactionsManager.revertImport(sessionId);
    }

    private void handleImportComplete(int sessionId, String uri) {
        int filteredCount = transactionsManager.completeImport(sessionId);
        if (filteredCount > 0) {
            this.appController
                    .showErrorWindow("Failed to import some transactions from: " + uri,
                            "Duplicated Transactions: " + filteredCount);
        }
    }

}
