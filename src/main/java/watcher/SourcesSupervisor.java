package watcher;

import com.google.inject.Singleton;
import io.reactivex.rxjava3.core.Observable;

import java.util.LinkedList;
import java.util.List;

@Singleton
public class SourcesSupervisor {

    private final List<SourceObserver> sourceObservers;

    public SourcesSupervisor() {
        sourceObservers = new LinkedList<>();
    }

    public Observable<SourceUpdate> checkForUpdates() {
        System.out.println(sourceObservers.size());
        return Observable
                .fromIterable(sourceObservers)
                .flatMap(SourceObserver::getChanges);
    }

    public void addSourceObserver(SourceObserver sourceObserver) {
        sourceObservers.add(sourceObserver);
    }

    public void removeSourceObserver(SourceObserver sourceObserver) {
        sourceObservers.remove(sourceObserver);
    }
}
