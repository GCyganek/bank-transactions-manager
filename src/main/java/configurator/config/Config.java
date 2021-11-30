package configurator.config;

import importer.utils.ParserField;

import java.util.List;

public class Config <K> implements ParserConfig<K>{
    private TransactionConfig<K> transactionConfig;
    private StatementConfig<K> statementConfig;

    public Config(TransactionConfig<K> transactionConfig, StatementConfig<K> statementConfig) {
        this.transactionConfig = transactionConfig;
        this.statementConfig = statementConfig;
    }

    public TransactionConfig<K> getTransactionConfig() {
        return transactionConfig;
    }

    public StatementConfig<K> getStatementConfig() {
        return statementConfig;
    }

    @Override
    public List<ParserField<K, ?>> getStatementFields() {
        return statementConfig.getFields();
    }

    @Override
    public List<ParserField<K, ?>> getTransactionFields() {
        return transactionConfig.getFields();
    }
}
