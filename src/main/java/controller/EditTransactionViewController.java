package controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;
import model.BankStatement;
import model.BankTransaction;
import model.TransactionCategory;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditTransactionViewController {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private Stage stage;

    private BigDecimal finalAmount;

    private final BankStatementsRepository bankStatementsRepository;

    private BankTransaction bankTransaction;

    @Inject
    public EditTransactionViewController(BankStatementsRepository bankStatementsRepository) {
        this.bankStatementsRepository = bankStatementsRepository;
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
        stage.close();
    }

    public void handleCancelAction(ActionEvent actionEvent) {
        finalAmount = bankTransaction.getAmount();
        stage.close();
    }

    private void updateBankTransaction() throws ParseException {
        bankTransaction.setDate(stringToDate());
        bankTransaction.setDescription(descriptionTextField.getText());
        bankTransaction.setAmount(stringToBigDecimal());
        bankTransaction.setCategory(categoryComboBox.getValue());

        bankStatementsRepository.updateTransaction(bankTransaction);

        if (checkStatementDateUpdateNeeded()) {
            bankStatementsRepository.updateStatement(bankTransaction.getBankStatement());
        }
    }

    private boolean checkStatementDateUpdateNeeded() {
        BankStatement bankStatement = bankTransaction.getBankStatement();
        LocalDate statementPeriodEndDate = bankStatement.getPeriodEndDate();
        LocalDate statementPeriodStartDate = bankStatement.getPeriodStartDate();
        LocalDate transactionDate = bankTransaction.getDate();

        if (transactionDate.isAfter(statementPeriodEndDate)) {
            bankStatement.setPeriodEndDate(transactionDate);
            return true;
        }

        if (transactionDate.isBefore(statementPeriodStartDate)) {
            bankStatement.setPeriodStartDate(transactionDate);
            return true;
        }

        return false;
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
