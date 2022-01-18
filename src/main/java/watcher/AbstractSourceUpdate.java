package watcher;

import model.util.BankType;
import model.util.DocumentType;

public abstract class AbstractSourceUpdate implements SourceUpdate{
    protected final BankType bankType;
    protected final DocumentType documentType;
    protected final SourceObserver sourceObserver;

    public AbstractSourceUpdate(SourceObserver sourceObserver, DocumentType documentType) {
        this.bankType = sourceObserver.getBankType();
        this.documentType = documentType;
        this.sourceObserver = sourceObserver;
    }

    @Override
    public DocumentType getDocumentType() {
        return documentType;
    }

    @Override
    public BankType getBankType() {
        return bankType;
    }

    @Override
    public SourceObserver getSourceObserver() {
        return sourceObserver;
    }
}