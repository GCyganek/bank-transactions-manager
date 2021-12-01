package model;

import configurator.config.StatementBuilderConfig;
import configurator.config.TransactionBuilderConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class BankStatementBuilder<K, U> {
    private final StatementBuilderConfig<K> statementConfig;
    private final TransactionBuilderConfig<U> transactionConfig;

    private BankStatement builtStatement;

    public BankStatementBuilder(StatementBuilderConfig<K> statementConfig,
                                TransactionBuilderConfig<U> transactionConfig)
    {
        this.statementConfig = statementConfig;
        this.transactionConfig = transactionConfig;
    }

    public BankTransaction buildBankTransaction(Map<U, ?> convertedTransaction) {
        String description = (String) convertedTransaction.get(transactionConfig.getDescriptionKey());
        BigDecimal amount = (BigDecimal) convertedTransaction.get(transactionConfig.getAmountKey());
        LocalDate date = (LocalDate) convertedTransaction.get(transactionConfig.getDateKey());
        BigDecimal balance = (BigDecimal) convertedTransaction.get(transactionConfig.getBalanceKey());

        BankTransaction bankTransaction = new BankTransaction(description, amount, date, balance);
        builtStatement.addBankTransaction(bankTransaction);

        return bankTransaction;
    }

    public BankStatement buildBankStatement(Map<K, ?> convertedStatement) {
        String accountNumber = (String)  convertedStatement.get(statementConfig.getAccountNumberKey());
        LocalDate periodStartDate = (LocalDate)  convertedStatement.get(statementConfig.getPeriodStartDateKey());
        LocalDate periodEndDate = (LocalDate)  convertedStatement.get(statementConfig.getPeriodEndDateKey());
        BigDecimal paidIn = (BigDecimal)  convertedStatement.get(statementConfig.getPaidInKey());
        BigDecimal paidOut = (BigDecimal)  convertedStatement.get(statementConfig.getPaidOutKey());
        String accountOwner = (String)  convertedStatement.get(statementConfig.getAccountOwnerKey());
        String currency = (String) convertedStatement.get(statementConfig.getCurrencyKey());

        builtStatement = new BankStatement(accountNumber, periodStartDate, periodEndDate,
                                        paidIn, paidOut, accountOwner, currency);
        return builtStatement;
    }
}
