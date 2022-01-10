package model.builder;

import configurator.config.StatementBuilderConfig;
import model.BankStatement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class BankStatementBuilder<K> {
    private final StatementBuilderConfig<K> config;

    public BankStatementBuilder(StatementBuilderConfig<K> config) {
        this.config = config;
    }


    public BankStatement buildBankStatement(Map<K, ?> convertedStatement) {
        String accountNumber = (String)  convertedStatement.get(config.getAccountNumberKey());
        LocalDate periodStartDate = (LocalDate)  convertedStatement.get(config.getPeriodStartDateKey());
        LocalDate periodEndDate = (LocalDate)  convertedStatement.get(config.getPeriodEndDateKey());
        BigDecimal paidIn = (BigDecimal)  convertedStatement.get(config.getPaidInKey());
        BigDecimal paidOut = (BigDecimal)  convertedStatement.get(config.getPaidOutKey());
        String accountOwner = (String)  convertedStatement.get(config.getAccountOwnerKey());
        String currency = (String) convertedStatement.get(config.getCurrencyKey());

        return new BankStatement(accountNumber, periodStartDate, periodEndDate,
                                        paidIn, paidOut, accountOwner, currency);
    }
}
