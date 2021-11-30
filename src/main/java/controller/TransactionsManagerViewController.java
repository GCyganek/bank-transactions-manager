package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.BankTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionsManagerViewController {

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
    public TableColumn<BankTransaction, String> statementIdColumn;

    @FXML
    public Button addButton;

    @FXML
    public void handleAddNewBankStatement(ActionEvent actionEvent) {

    }
}
