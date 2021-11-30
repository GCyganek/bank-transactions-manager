package configurator;

import configurator.config.Config;
import importer.BankParser;
import model.BankStatementBuilder;
import model.BankType;
import model.DocumentType;
import repository.BankStatementsRepository;
import java.util.HashSet;

public abstract class AbstractBankConfigurator implements BankConfigurator{
    protected BankType bankType;
    protected BankStatementsRepository repository;
    protected HashSet<DocumentType> supportedDocumentTypes;

    public AbstractBankConfigurator(BankType bankType, BankStatementsRepository repository) {
        this.bankType = bankType;
        this.repository = repository;
        this.supportedDocumentTypes = new HashSet<>();
    }

    @Override
    public BankParser<?, ?> getConfiguredParser(DocumentType documentType) {
        if (!supportedDocumentTypes.contains(documentType))
            throw new UnsupportedOperationException(
                    String.format("%s is not supported for %s\n", documentType, bankType));

        return configureParser(getConfig(documentType));
    }

    protected abstract Config<?, ?> getConfig(DocumentType documentType);

    private <K, U> BankParser<K, U> configureParser(Config<K, U> config) {
        BankStatementBuilder<K, U> bankStatementBuilder =
                new BankStatementBuilder<>(repository,
                        config.getStatementConfig(),
                        config.getTransactionConfig());

        return new BankParser<>(config, bankStatementBuilder);
    }
}
