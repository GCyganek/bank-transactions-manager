package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;
import model.BankTransaction;
import model.TransactionsSupervisor;
import model.util.TransactionCategory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditTransactionViewPresenter {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private Stage stage;

    private BankTransaction bankTransaction;

    private final TransactionsSupervisor transactionsSupervisor;

    private BigDecimal finalAmount;

    private boolean editApproved = false;

    private TransactionsManagerAppController appController;

    @Inject
    public EditTransactionViewPresenter(TransactionsSupervisor transactionsSupervisor) {
        this.transactionsSupervisor = transactionsSupervisor;
    }

    @FXML
    public TextField amountTextField;

    @FXML
    public TextField descriptionTextField;

    @FXML
    public TextField dateTextField;

    @FXML
    public ComboBox<TransactionCategory> categoryComboBox;

    @FXML
    public Button okButton;

    @FXML
    public Button cancelButton;

    public BigDecimal getFinalAmount() { return finalAmount; }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setData(BankTransaction bankTransaction) {
        this.bankTransaction = bankTransaction;
        updateTextFields();
        updateCategoryComboBox();
    }

    public boolean isEditApproved() {
        return editApproved;
    }

    private void updateCategoryComboBox() {
        categoryComboBox.getItems().addAll(TransactionCategory.values());
        categoryComboBox.getSelectionModel().select(bankTransaction.getCategory());
    }

    private void updateTextFields() {
        dateTextField.setText(dateToString());
        descriptionTextField.setText(bankTransaction.getDescription());
        amountTextField.setText(bankTransaction.getAmount().toString());
    }

    public void handleOkAction(ActionEvent actionEvent) {
        try {
            if (!updateBankTransaction()) return;
        } catch (ParseException e) {
            appController.showErrorWindow(
                    "Failed to update transaction. Make sure that the new transaction amount is a valid number",
                            e.getMessage()
            );
            return;
        }

        finalAmount = bankTransaction.getAmount();
        editApproved = true;
        stage.close();
    }

    public void handleCancelAction(ActionEvent actionEvent) {
        stage.close();
    }

    /**
     * @return false if update did not succeed and <code>EditTransactionView</code> should remain open for further
     * transaction's edit
     * @throws ParseException if parsing <code>amountTextField</code> string value to <code>BigDecimal</code> failed in
     * <code>getEditedAmount()</code> method
     */
    private boolean updateBankTransaction() throws ParseException {
        BankTransaction editedTransaction = getEditedTransaction();

        if (editedTransaction.equals(bankTransaction))
            return true;

        if (!transactionsSupervisor.updateTransaction(bankTransaction, editedTransaction)) {
            appController.showErrorWindow("Failed to update transaction.", "Transaction with these fields already exits");
            return false;
        }
        return true;
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }

    private BankTransaction getEditedTransaction() throws ParseException {
        BankTransaction editedTransaction = bankTransaction.shallowCopy();

        editedTransaction.setDescription(descriptionTextField.getText());
        editedTransaction.setAmount(getEditedAmount());
        editedTransaction.setDate(getEditedDate());
        editedTransaction.setCategory(categoryComboBox.getValue());

        return editedTransaction;
    }

    private String dateToString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateStringConverter converter = new LocalDateStringConverter(formatter, formatter);
        return converter.toString(bankTransaction.getDate());
    }

    private LocalDate getEditedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateStringConverter converter = new LocalDateStringConverter(formatter, formatter);
        return converter.fromString(dateTextField.getText());
    }

    private BigDecimal getEditedAmount() throws ParseException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        String pattern = "#,##0.00";
        DecimalFormat decimalFormatter = new DecimalFormat(pattern, symbols);
        decimalFormatter.setParseBigDecimal(true);
        return (BigDecimal) decimalFormatter.parse(amountTextField.getText());
    }

}
