package controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;
import model.BankTransaction;
import model.TransactionCategory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditTransactionViewController {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private Stage stage;

    private BankTransaction bankTransaction;

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
        updateBankTransaction();
        stage.close();
    }

    public void handleCancelAction(ActionEvent actionEvent) {
        stage.close();
    }

    private void updateBankTransaction() {

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
}
