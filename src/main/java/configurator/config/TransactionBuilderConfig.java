package configurator.config;

import importer.utils.ParserField;
import importer.utils.converters.Converter;

import java.util.LinkedList;
import java.util.List;

public class TransactionBuilderConfig<K>{
    private final List<ParserField<K, ?>> transactionFields;

    private K descriptionKey;
    private K amountKey;
    private K dateKey;

    public TransactionBuilderConfig() {
        transactionFields = new LinkedList<>();
    }

    public K getDescriptionKey() {
        return descriptionKey;
    }

    public K getAmountKey() {
        return amountKey;
    }

    public K getDateKey() {
        return dateKey;
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

    private void addTransactionField(K key, Converter<?> converter) {
        transactionFields.add(new ParserField<>(key, converter));
    }

    public List<ParserField<K, ?>> getFields() {
        return transactionFields;
    }
}