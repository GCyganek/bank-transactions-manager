package controller;

import importer.exceptions.ParserException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.BankTransaction;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionsManagerViewController {

    private final ObservableList<BankTransaction> bankTransactions = FXCollections.observableArrayList();

    private final BankStatementsRepository bankStatementsRepository;


    private TransactionsManagerAppController appController;

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
    public TableColumn<BankTransaction, BigDecimal> balanceColumn;

    @FXML
    public Button addButton;

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(dataValue -> dataValue.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(dataValue -> dataValue.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(dataValue -> dataValue.getValue().amountProperty());
        balanceColumn.setCellValueFactory(dataValue -> dataValue.getValue().balanceProperty());
    }

    public void fetchDataFromDatabase() {
        bankTransactions.addAll(bankStatementsRepository.getAllTransactions());
        transactionsTable.setItems(bankTransactions);
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    public void handleAddNewBankStatement(ActionEvent actionEvent) {
        appController.showAddStatementView()
                .subscribeOn(Schedulers.io())
                .subscribe(bankTransaction -> { //TODO do it on Fx scheduler
                    Platform.runLater(() -> {
                        bankTransactions.add(bankTransaction);
                        System.out.println("Imported Transaction: " + bankTransaction);
                    });
                }, err -> {
                    Platform.runLater(() -> {
                        String reason = "";
                        if (err instanceof ParserException e) {
                            reason = e.getReason();
                        }
                        this.appController.showErrorWindow(err.getMessage(), reason);
                    });
                });
    }



}
