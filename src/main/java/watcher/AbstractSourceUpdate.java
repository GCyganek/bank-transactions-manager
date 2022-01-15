package watcher;

import model.util.BankType;
import model.util.DocumentType;

public abstract class AbstractSourceUpdate implements SourceUpdate{
    private final BankType bankType;
    protected DocumentType documentType;

    public AbstractSourceUpdate(BankType bankType) {
        this.bankType = bankType;
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