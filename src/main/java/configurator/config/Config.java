package configurator.config;

import importer.raw.RawDataParser;
import importer.utils.ParserField;

import java.util.List;

public class Config<K, U> implements ParserConfig<K, U> {
    private final StatementConfig<K> statementConfig;
    private final TransactionConfig<U> transactionConfig;
    private final RawDataParser<K, U> rawDataParser;

    public Config(RawDataParser<K, U> rawDataParser,
                  StatementConfig<K> statementConfig,
                  TransactionConfig<U> transactionConfig) {
        this.rawDataParser = rawDataParser;
        this.transactionConfig = transactionConfig;
        this.statementConfig = statementConfig;
    }

    public StatementConfig<K> getStatementConfig() {
        return statementConfig;
    }

    public TransactionConfig<U> getTransactionConfig() {
        return transactionConfig;
    }


    @Override
    public List<ParserField<K, ?>> getStatementFields() {
        return statementConfig.getFields();
    }

    @Override
    public List<ParserField<U, ?>> getTransactionFields() {
        return transactionConfig.getFields();
    }

    @Override
    public RawDataParser<K, U> getRawDataParser() {
        return rawDataParser;
    }
}
