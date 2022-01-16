package watcher;

import com.google.inject.Singleton;
import io.reactivex.rxjava3.core.Observable;

import java.util.HashSet;
import java.util.Set;

@Singleton
public class SourcesSupervisor {

    private final Set<SourceObserver> sourceObservers;

    public SourcesSupervisor() {
        sourceObservers = new HashSet<>();
    }

    public Observable<SourceUpdate> checkForUpdates() {
        System.out.println(sourceObservers.size());
        return Observable
                .fromIterable(sourceObservers)
                .filter(sourceObserver -> sourceObserver.activeProperty().getValue())
                .flatMap(SourceObserver::getChanges);
    }

    public void addSourceObserver(SourceObserver sourceObserver) {
        sourceObservers.add(sourceObserver);
    }

    public void removeSourceObserver(SourceObserver sourceObserver) {
        sourceObservers.remove(sourceObserver);
    }
}
