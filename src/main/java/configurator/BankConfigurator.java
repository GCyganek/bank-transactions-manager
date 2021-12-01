package configurator;

import importer.BankParser;
import model.DocumentType;

public interface BankConfigurator {
    /**
     * @return BankParser configured to parse file with given extension provided by configurator's bank.
     *         UnsupportedOperationException is thrown if bank doesn't support that documentType.
     */
    BankParser<?, ?> getConfiguredParser(DocumentType documentType);
}
