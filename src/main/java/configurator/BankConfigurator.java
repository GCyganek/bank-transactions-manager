package configurator;

import importer.BankParser;
import model.DocumentType;

public interface BankConfigurator {
    BankParser<?> configureParser(DocumentType documentType);
}
