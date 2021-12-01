package configurator.config;

import importer.utils.ParserField;
import importer.utils.converters.Converter;

import java.util.LinkedList;
import java.util.List;

public class TransactionConfig<K> implements TransactionBuilderConfig<K> {
    private final List<ParserField<K, ?>> transactionFields;

    private K descriptionKey;
    private K amountKey;
    private K dateKey;
    private K balanceKey;

    public TransactionConfig() {
        transactionFields = new LinkedList<>();
    }

    @Override
    public K getDescriptionKey() {
        return descriptionKey;
    }

    @Override
    public K getAmountKey() {
        return amountKey;
    }

    @Override
    public K getDateKey() {
        return dateKey;
    }

    @Override
    public K getBalanceKey() {
        return balanceKey;
    }

    public void setDescriptionKey(K descriptionKey, Converter<?> converter) {
        this.descriptionKey = descriptionKey;
        addTransactionField(descriptionKey, converter);
    }

    public void setAmountKey(K amountKey, Converter<?> converter) {
        this.amountKey = amountKey;
        addTransactionField(amountKey, converter);
    }

    public void setDateKey(K dateKey, Converter<?> converter) {
        this.dateKey = dateKey;
        addTransactionField(dateKey, converter);
    }

    public void setBalanceKey(K balanceKey, Converter<?> converter) {
        this.balanceKey = balanceKey;
        addTransactionField(balanceKey, converter);
    }


    private void addTransactionField(K key, Converter<?> converter) {
        transactionFields.add(new ParserField<>(key, converter));
    }


    public List<ParserField<K, ?>> getFields() {
        return transactionFields;
    }
}