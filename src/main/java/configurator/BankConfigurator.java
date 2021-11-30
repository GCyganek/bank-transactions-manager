package configurator;

import importer.BankParser;
import model.DocumentType;

public interface BankConfigurator {
    BankParser<?, ?> getConfiguredParser(DocumentType documentType);
}
