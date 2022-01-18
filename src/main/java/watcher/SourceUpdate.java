package watcher;

import importer.loader.Loader;
import io.reactivex.rxjava3.core.Single;
import model.util.BankType;
import model.util.DocumentType;

public interface SourceUpdate {
    Single<Loader> getUpdateDataLoader();
    BankType getBankType();
    DocumentType getDocumentType();

    SourceObserver getSourceObserver();
}
