package watcher;

import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import model.util.BankType;
import model.util.DocumentType;

public class DirectorySourceUpdate extends AbstractSourceUpdate {
    private final String path;

    public DirectorySourceUpdate(BankType bankType, String path) {
        super(bankType);
        this.path = path;
    }

    @Override
    public Single<Loader> executeUpdate() {
        this.documentType = DocumentType.CSV; //TODO
        return Single.just(new LocalFSLoader(path));
    }
}