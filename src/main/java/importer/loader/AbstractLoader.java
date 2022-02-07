package importer.loader;

import model.util.BankType;
import model.util.DocumentType;

public abstract class AbstractLoader implements Loader{
    protected final String description;
    protected final BankType bankType;
    protected final DocumentType documentType;

    public AbstractLoader(String description, BankType bankType, DocumentType documentType) {
        this.description = description;
        this.bankType = bankType;
        this.documentType = documentType;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public BankType getBankType() {
        return bankType;
    }

    @Override
    public DocumentType getDocumentType() {
        return documentType;
    }
}
