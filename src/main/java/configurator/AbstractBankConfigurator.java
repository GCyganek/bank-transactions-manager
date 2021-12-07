package configurator;

import configurator.config.util.ConfigWrapper;
import configurator.config.StatementBuilderConfig;
import configurator.config.TransactionBuilderConfig;
import importer.BankParser;
import importer.raw.RawDataParser;
import model.BankStatementBuilder;
import model.BankTransactionBuilder;
import model.BankType;
import model.DocumentType;
import java.util.HashSet;

public abstract class AbstractBankConfigurator implements BankConfigurator {
    protected BankType bankType;
    protected HashSet<DocumentType> supportedDocumentTypes;

    public AbstractBankConfigurator(BankType bankType) {
        this.bankType = bankType;
        this.supportedDocumentTypes = new HashSet<>();
    }

    @Override
    public BankParser<?, ?> getConfiguredParser(DocumentType documentType) {
        return configureParser(getConfig(documentType));
    }

    protected UnsupportedOperationException getInvalidDocumentTypeError(DocumentType documentType) {
        return new UnsupportedOperationException(
                String.format("%s is not supported for %s\n", documentType, bankType));
    }

    protected abstract ConfigWrapper<?, ?> getConfig(DocumentType documentType);

    private <K, U> BankParser<K, U> configureParser(ConfigWrapper<K, U> config) {
        StatementBuilderConfig<K> statementBuilderConfig = config.statementBuilderConfig();
        TransactionBuilderConfig<U> transactionBuilderConfig = config.transactionBuilderConfig();
        RawDataParser<K, U> rawDataParser = config.rawDataParser();

        BankStatementBuilder<K> bankStatementBuilder = new BankStatementBuilder<>(config.statementBuilderConfig());

        BankTransactionBuilder<U> bankTransactionBuilder = new BankTransactionBuilder<>(config.transactionBuilderConfig());

        return new BankParser<>(rawDataParser, statementBuilderConfig.getFields(), transactionBuilderConfig.getFields(),
                bankStatementBuilder, bankTransactionBuilder);
    }
}
