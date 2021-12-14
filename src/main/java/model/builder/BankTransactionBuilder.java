package model.builder;

import configurator.config.TransactionBuilderConfig;
import model.BankStatement;
import model.BankTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class BankTransactionBuilder<K> {
    private final TransactionBuilderConfig<K> config;

    public BankTransactionBuilder(TransactionBuilderConfig<K> config) {
        this.config = config;
    }

    public BankTransaction buildBankTransaction(BankStatement bankStatement, Map<K, ?> convertedTransaction) {
        String description = (String) convertedTransaction.get(config.getDescriptionKey());
        BigDecimal amount = (BigDecimal) convertedTransaction.get(config.getAmountKey());
        LocalDate date = (LocalDate) convertedTransaction.get(config.getDateKey());

        BankTransaction bankTransaction = new BankTransaction(description, amount, date);
        bankTransaction.setBankStatement(bankStatement);

        return bankTransaction;
    }
}
