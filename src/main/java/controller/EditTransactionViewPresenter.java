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

    private final BankStatementsRepository bankStatementsRepository;

    private BigDecimal finalAmount;

    private boolean editApproved = false;

    @Inject
    public EditTransactionViewPresenter(BankStatementsRepository bankStatementsRepository) {
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
        BigDecimal amountBeforeEdit = bankTransaction.getAmount();

        bankTransaction.setDate(stringToDate());
        bankTransaction.setDescription(descriptionTextField.getText());
        bankTransaction.setAmount(stringToBigDecimal());
        bankTransaction.setCategory(categoryComboBox.getValue());

        bankStatementsRepository.updateTransaction(bankTransaction);

        if (checkStatementPaidInOutUpdateNeeded(amountBeforeEdit) || checkStatementDateUpdateNeeded()) {
            bankStatementsRepository.updateStatement(bankTransaction.getBankStatement());
        }
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

    private boolean checkStatementPaidInOutUpdateNeeded(BigDecimal amountBeforeEdit) {
        BigDecimal amountAfterEdit = bankTransaction.getAmount();

        if (amountAfterEdit.compareTo(amountBeforeEdit) == 0) return false;

        BankStatement bankStatement = bankTransaction.getBankStatement();

        if (amountAfterEdit.compareTo(BigDecimal.ZERO) > 0) {
            if (amountBeforeEdit.compareTo(BigDecimal.ZERO) > 0) {
                bankStatement.addToPaidIn(amountAfterEdit.subtract(amountBeforeEdit));
            } else {
                bankStatement.addToPaidOut(amountBeforeEdit.negate());
                bankStatement.addToPaidIn(amountAfterEdit);
            }
        }

        else  {
            if (amountBeforeEdit.compareTo(BigDecimal.ZERO) >= 0) {
                bankStatement.addToPaidIn(amountBeforeEdit.negate());
                bankStatement.addToPaidOut(amountAfterEdit);
            } else {
                bankStatement.addToPaidOut(amountAfterEdit.subtract(amountBeforeEdit));
            }
        }

        return true;
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
}
