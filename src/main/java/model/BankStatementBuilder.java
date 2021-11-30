package model;

import configurator.config.StatementBuilderConfig;
import configurator.config.TransactionBuilderConfig;
import repository.BankStatementsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BankStatementBuilder<K, U> {
    private final StatementBuilderConfig<K> statementConfig;
    private final TransactionBuilderConfig<U> transactionConfig;
    private final BankStatementsRepository statementsRepo;

    private List<BankTransaction> builtTransactions;
    private BankStatement builtStatement;

    public BankStatementBuilder(BankStatementsRepository statementsRepository,
                                StatementBuilderConfig<K> statementConfig,
                                TransactionBuilderConfig<U> transactionConfig)
    {
        this.statementsRepo = statementsRepository;
        this.statementConfig = statementConfig;
        this.transactionConfig = transactionConfig;
        this.builtTransactions = new LinkedList<>();
    }

    public BankStatementBuilder<K, U> buildBankTransaction(Map<U, ?> convertedTransaction) {
        String description = (String) convertedTransaction.get(transactionConfig.getDescriptionKey());
        BigDecimal amount = (BigDecimal) convertedTransaction.get(transactionConfig.getAmountKey());
        LocalDate date = (LocalDate) convertedTransaction.get(transactionConfig.getDateKey());
        BigDecimal balance = (BigDecimal) convertedTransaction.get(transactionConfig.getBalanceKey());

        BankTransaction bankTransaction = new BankTransaction(description, amount, date, balance);
        builtTransactions.add(bankTransaction);
        return this;
    }

    public BankStatementBuilder<K, U> buildBankStatement(Map<K, ?> convertedStatement) {
        String accountNumber = (String)  convertedStatement.get(statementConfig.getAccountNumberKey());
        LocalDate periodStartDate = (LocalDate)  convertedStatement.get(statementConfig.getPeriodStartDateKey());
        LocalDate periodEndDate = (LocalDate)  convertedStatement.get(statementConfig.getPeriodEndDateKey());
        BigDecimal paidIn = (BigDecimal)  convertedStatement.get(statementConfig.getPaidInKey());
        BigDecimal paidOut = (BigDecimal)  convertedStatement.get(statementConfig.getPaidOutKey());
        String accountOwner = (String)  convertedStatement.get(statementConfig.getAccountOwnerKey());

        builtStatement = new BankStatement(accountNumber, periodStartDate, periodEndDate,
                                        paidIn, paidOut, accountOwner);
        return this;
    }

    public BankStatement build() {
        return statementsRepo.addStatementWithTransactions(builtStatement, builtTransactions);
    }

}
