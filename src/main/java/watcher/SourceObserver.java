package watcher;

import io.reactivex.rxjava3.core.Observable;

import java.nio.file.Path;

public interface SourceObserver {
    Observable<SourceUpdate> getChanges();
    SourceType getType();

}