package watcher;

import importer.loader.Loader;
import io.reactivex.rxjava3.core.Observable;

public interface SourceUpdate {
    Observable<Loader> executeUpdate();
}
