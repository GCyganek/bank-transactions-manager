package controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;
import jdk.jfr.Category;
import model.BankStatement;
import model.BankTransaction;
import model.TransactionsManager;
import model.util.TransactionCategory;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditTransactionViewPresenter {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private Stage stage;

    private BankTransaction bankTransaction;

    private final TransactionsManager transactionsManager;

    private BigDecimal finalAmount;

    private boolean editApproved = false;

    private TransactionsManagerAppController appController;

    @Inject
    public EditTransactionViewPresenter(TransactionsManager transactionsManager) {
        this.transactionsManager = transactionsManager;
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
            updateBankTransaction();
        } catch (ParseException e) {
            System.out.println("Error while parsing string to BigDecimal: " + e.getMessage());
        }

        finalAmount = bankTransaction.getAmount();
        editApproved = true;
        stage.close();
    }

    public void handleCancelAction(ActionEvent actionEvent) {
        stage.close();
    }

    private void updateBankTransaction() throws ParseException {
            LocalDate editedDate = stringToDate();
            String editedDescription = descriptionTextField.getText();
            BigDecimal editedAmount = stringToBigDecimal();
            TransactionCategory editedCategory = categoryComboBox.getValue();

            BankTransaction editedTransaction = new BankTransaction(editedDescription, editedAmount, editedDate);
            editedTransaction.setCategory(editedCategory);
            editedTransaction.setBankStatement(bankTransaction.getBankStatement());

            if (editedTransaction.equals(bankTransaction))
                return;

            if (!transactionsManager.updateTransaction(bankTransaction, editedTransaction)) {
                appController.showErrorWindow("Failed to update transaction.", "Transaction with these fields already exits");
            }
    }

    public void setAppController(TransactionsManagerAppController appController) {
        this.appController = appController;
    }


    private String dateToString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateStringConverter converter = new LocalDateStringConverter(formatter, formatter);
        return converter.toString(bankTransaction.getDate());
    }

    private LocalDate stringToDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateStringConverter converter = new LocalDateStringConverter(formatter, formatter);
        return converter.fromString(dateTextField.getText());
    }

    private BigDecimal stringToBigDecimal() throws ParseException {
        DecimalFormat decimalFormatter = new DecimalFormat();
        decimalFormatter.setParseBigDecimal(true);
        return (BigDecimal) decimalFormatter.parse(amountTextField.getText());
    }



}
