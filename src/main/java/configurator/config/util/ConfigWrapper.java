package configurator.config.util;

import configurator.config.StatementBuilderConfig;
import configurator.config.TransactionBuilderConfig;
import importer.raw.RawDataParser;

public record ConfigWrapper<K, U>(RawDataParser<K, U> rawDataParser,
                                  StatementBuilderConfig<K> statementBuilderConfig,
                                  TransactionBuilderConfig<U> transactionBuilderConfig) {

}
