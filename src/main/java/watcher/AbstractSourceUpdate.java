package watcher;

import model.util.BankType;
import model.util.DocumentType;

public abstract class AbstractSourceUpdate implements SourceUpdate{
    private final BankType bankType;
    protected final DocumentType documentType;

    public AbstractSourceUpdate(BankType bankType, DocumentType documentType) {
        this.bankType = bankType;
        this.documentType = documentType;
    }

    @Override
    public DocumentType getDocumentType() {
        return documentType;
    }

    @Override
    public BankType getBankType() {
        return bankType;
    }
}