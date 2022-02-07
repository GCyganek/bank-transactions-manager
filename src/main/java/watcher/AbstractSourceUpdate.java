package watcher;

import model.util.BankType;
import model.util.DocumentType;

import java.time.LocalDateTime;

public abstract class AbstractSourceUpdate implements SourceUpdate{
    protected final BankType bankType;
    protected final DocumentType documentType;
    protected final SourceObserver sourceObserver;
    protected final LocalDateTime updateCheckTime;

    public AbstractSourceUpdate(SourceObserver sourceObserver, DocumentType documentType, LocalDateTime updateCheckTime) {
        this.bankType = sourceObserver.getBankType();
        this.documentType = documentType;
        this.sourceObserver = sourceObserver;
        this.updateCheckTime = updateCheckTime;
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

    @Override
    public LocalDateTime getUpdateCheckTime() {
        return updateCheckTime;
    }
}