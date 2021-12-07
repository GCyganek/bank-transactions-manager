package configurator.config;

import importer.utils.ParserField;
import importer.utils.converters.Converter;

import java.util.LinkedList;
import java.util.List;


public class StatementBuilderConfig<K> {
    private final List<ParserField<K, ?>> statementFields;

    private K accountNumberKey;
    private K periodStartDateKey;
    private K periodEndDateKey;
    private K paidInKey;
    private K paidOutKey;
    private K currencyKey;

    public K getAccountNumberKey() {
        return accountNumberKey;
    }

    public K getPeriodStartDateKey() {
        return periodStartDateKey;
    }

    public K getPeriodEndDateKey() {
        return periodEndDateKey;
    }

    public K getPaidInKey() {
        return paidInKey;
    }

    public K getPaidOutKey() {
        return paidOutKey;
    }

    public K getAccountOwnerKey() {
        return accountOwnerKey;
    }

    public K getCurrencyKey() {
        return currencyKey;
    }

    private K accountOwnerKey;

    public StatementBuilderConfig() {
        statementFields = new LinkedList<>();
    }

    public void setAccountNumberKey(K accountNumberKey, Converter<?> converter) {
        this.accountNumberKey = accountNumberKey;
        addStatementField(accountNumberKey, converter);
    }

    public void setPeriodStartDateKey(K periodStartDateKey, Converter<?> converter) {
        this.periodStartDateKey = periodStartDateKey;
        addStatementField(periodStartDateKey, converter);
    }

    public void setPeriodEndDateKey(K periodEndDateKey, Converter<?> converter) {
        this.periodEndDateKey = periodEndDateKey;
        addStatementField(periodEndDateKey, converter);
    }

    public void setPaidInKey(K paidInKey, Converter<?> converter) {
        this.paidInKey = paidInKey;
        addStatementField(paidInKey, converter);
    }

    public void setPaidOutKey(K paidOutKey, Converter<?> converter) {
        this.paidOutKey = paidOutKey;
        addStatementField(paidOutKey, converter);
    }

    public void setAccountOwnerKey(K accountOwnerKey, Converter<?> converter) {
        this.accountOwnerKey = accountOwnerKey;
        addStatementField(accountOwnerKey, converter);
    }

    public void setCurrencyKey(K currencyKey, Converter<?> converter) {
        this.currencyKey = currencyKey;
        addStatementField(currencyKey, converter);
    }

    private void addStatementField(K key, Converter<?> converter) {
        statementFields.add(new ParserField<>(key, converter));
    }


    public List<ParserField<K, ?>> getFields() {
        return statementFields;
    }
}
