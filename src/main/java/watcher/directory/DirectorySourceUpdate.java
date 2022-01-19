package watcher.directory;

import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import model.util.BankType;
import model.util.DocumentType;
import watcher.AbstractSourceUpdate;
import watcher.SourceObserver;

import java.time.LocalDateTime;

public class DirectorySourceUpdate extends AbstractSourceUpdate {
    private final String path;

    public DirectorySourceUpdate(DirectoryObserver sourceObserver, DocumentType documentType,
                                 String path, LocalDateTime updateTime)
    {
        super(sourceObserver, documentType, updateTime);
        this.path = path;
    }

    @Override
    public Single<Loader> getUpdateDataLoader() {
        return Single.just(new LocalFSLoader(path, bankType, documentType));
    }
}