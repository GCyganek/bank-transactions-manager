package controller;

import controller.util.ContextMenuRowFactory;
import importer.Importer;
import importer.exceptions.ParserException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.scene.control.*;
import model.TransactionsManager;
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
    public Button statsButton;

    @FXML
    public TextField balanceTextField;

    @FXML
    public ContextMenu contextMenu;

    @FXML
    private void initialize() {
        transactionsTable.setItems(bankTransactions);

        transactionsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        balanceTextField.textProperty().bind(transactionsManager
                .balanceProperty().asString("Transactions Balance: %.2f"));

        dateColumn.setCellValueFactory(dataValue -> dataValue.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(dataValue -> dataValue.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(dataValue -> dataValue.getValue().amountProperty());
        categoryColumn.setCellValueFactory(dataValue -> dataValue.getValue().categoryProperty());

        editButton.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        contextMenu.setStyle("-fx-min-width: 120.0; -fx-min-height: 40.0;");
        transactionsTable.setRowFactory(new ContextMenuRowFactory<>(contextMenu));
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
        ImportSession importSession = transactionsManager.startImportSession();

        try {
            importer.importBankStatement(selectedBank, selectedDocType, uri)
                    .subscribeOn(Schedulers.io())
                    .filter(transaction -> transactionsManager.tryToAddTransaction(importSession, transaction))
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(transactionsManager::addToView,
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
        this.transactionsManager.reverseImport(importSession);
    }

    private void handleImportComplete(ImportSession importSession, String uri) {
        int filteredCount = transactionsManager.completeImport(importSession);
        if (filteredCount > 0) {
            this.appController
                    .showErrorWindow("Failed to import some transactions from: " + uri,
                            "Duplicated Transactions: " + filteredCount);
        }
    }

}
