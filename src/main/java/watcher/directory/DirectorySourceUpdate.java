package watcher.directory;

import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import model.util.BankType;
import model.util.DocumentType;
import watcher.AbstractSourceUpdate;

public class DirectorySourceUpdate extends AbstractSourceUpdate {
    private final String path;

    public DirectorySourceUpdate(BankType bankType, DocumentType documentType, String path) {
        super(bankType, documentType);
        this.path = path;
    }

    @Override
    public Single<Loader> getUpdateDataLoader() {
        return Single.just(new LocalFSLoader(path));
    }
}