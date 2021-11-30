package configurator.config;

public interface TransactionBuilderConfig<K> {
    K getDescriptionKey();

    K getAmountKey();

    K getDateKey();

    K getBalanceKey();
}
