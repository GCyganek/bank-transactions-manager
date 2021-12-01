package configurator.config;

public interface StatementBuilderConfig<K> {
    K getAccountNumberKey();

    K getPeriodStartDateKey();

    K getPeriodEndDateKey();

    K getPaidInKey();

    K getPaidOutKey();

    K getAccountOwnerKey();

    K getCurrencyKey();
}
