package controller;

import importer.exceptions.ParserException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.BankTransaction;
import model.TransactionCategory;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionsManagerViewController {

    private final ObservableList<BankTransaction> bankTransactions = FXCollections.observableArrayList();

    private final BankStatementsRepository bankStatementsRepository;

    private TransactionsManagerAppController appController;

    private BigDecimal balance = BigDecimal.ZERO;

    @Inject
    public TransactionsManagerViewController(BankStatementsRepository bankStatementsRepository) {
        this.bankStatementsRepository = bankStatementsRepository;
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
        dateColumn.setCellValueFactory(dataValue -> dataValue.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(dataValue -> dataValue.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(dataValue -> dataValue.getValue().amountProperty());
        categoryColumn.setCellValueFactory(dataValue -> dataValue.getValue().categoryProperty());

        editButton.disableProperty().bind(Bindings.size(transactionsTable.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
    }

    private void addToBalance(BigDecimal amount) {
        balance = balance.add(amount);
    }

    private void updateBalanceTextView() {
        balanceTextField.setText("Transactions Balance: " + balance);
    }

    public void fetchDataFromDatabase() {
        bankTransactions.addAll(bankStatementsRepository.getAllTransactions());
        transactionsTable.setItems(bankTransactions);
        bankTransactions.forEach(bankTransaction -> addToBalance(bankTransaction.getAmount()));
        updateBalanceTextView();
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
                    addToBalance(amountAfterEdit.subtract(amountBeforeEdit));
                    updateBalanceTextView();
                }
            });
        }
    }

    public void handleAddNewBankStatement(ActionEvent actionEvent) {
        appController.showAddStatementView()
                .subscribeOn(Schedulers.io())
                .subscribe(bankTransaction -> { //TODO do it on Fx scheduler
                    Platform.runLater(() -> {
                        bankTransactions.add(bankTransaction);
                        System.out.println("Imported Transaction: " + bankTransaction);
                        addToBalance(bankTransaction.getAmount());
                    });
                }, err -> Platform.runLater(() -> {
                    String reason = "";
                    if (err instanceof ParserException e) {
                        reason = e.getReason();
                    }
                    this.appController.showErrorWindow(err.getMessage(), reason);
                }), () -> Platform.runLater(this::updateBalanceTextView));
    }

}
