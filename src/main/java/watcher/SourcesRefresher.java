package watcher;

import com.google.inject.Singleton;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import watcher.exceptions.DuplicateSourceException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class SourcesRefresher {

    private final ObservableList<SourceObserver> sourceObservers;
    private final ObservableList<SourceUpdate> availableUpdates;

    private boolean isCheckingPeriodically;
    private boolean periodicalChecksDisabled;

    private long period;
    private TimeUnit timeUnit;

    public SourcesRefresher() {
        sourceObservers = FXCollections.observableArrayList();
        availableUpdates = FXCollections.observableArrayList();
        isCheckingPeriodically = false;
        periodicalChecksDisabled = false;
    }


    public Observable<SourceUpdate> getUpdates() {
        periodicalChecksDisabled = true;
        List<SourceUpdate> sourceUpdates = List.copyOf(availableUpdates);

        availableUpdates.clear();

        return Observable
                .merge(
                    Observable.fromIterable(sourceUpdates),
                    checkForUpdates()
                )
                .doOnTerminate(() -> periodicalChecksDisabled = false);
    }

    public IntegerBinding getAvailableUpdatesCount() {
        return Bindings.size(availableUpdates);
    }


    public void startPeriodicalUpdateChecks(long period, TimeUnit timeUnit) {
        this.period = period;
        this.timeUnit = timeUnit;

        if (isCheckingPeriodically)
            return;

        isCheckingPeriodically = true;

        setupTimer();
    }

    private void setupTimer() {
        Observable
                .timer(period, timeUnit)
                .filter(tick -> isCheckingPeriodically)
                .subscribe(tick -> {
                    System.out.println("TICK");
                    // recursive call instead of Observable.interval so time needed
                    // to execute this will be included
                    if (!periodicalChecksDisabled)
                        cacheUpdates();
                    else
                        setupTimer();
                });
    }

    public void stopPeriodicalUpdateChecks() {
        isCheckingPeriodically = false;
    }

    private Observable<SourceUpdate> checkForUpdates() {
        return Observable
                .fromIterable(sourceObservers)
                .filter(SourceObserver::isActive)
                .flatMap(SourceObserver::getChanges);
    }

    private void cacheUpdates() {
        checkForUpdates()
                .subscribeOn(Schedulers.io())
//                .delay(period + 1, TimeUnit.SECONDS)
                .observeOn(JavaFxScheduler.platform())
                .subscribe(availableUpdates::add,
                        System.out::println,
                        this::setupTimer);
    }

    public ObservableList<SourceObserver> getSourceObservers() {
        return sourceObservers;
    }


    public void deactivateSource(SourceObserver sourceObserver) {
        sourceObserver.setActive(false);
    }

    public void reactivateSource(SourceObserver sourceObserver) {
        sourceObserver.setActive(true);
    }

    public void addSourceObserver(SourceObserver sourceObserver) throws DuplicateSourceException {
        if (isDuplicated(sourceObserver))
            throw new DuplicateSourceException(sourceObserver.getDescription());

        sourceObservers.add(sourceObserver);
    }

    public void removeSourceObserver(SourceObserver sourceObserver) {
        deactivateSource(sourceObserver);
        sourceObservers.remove(sourceObserver);
        availableUpdates.removeIf(sourceUpdate -> sourceUpdate.getSourceObserver().equals(sourceObserver));
    }

    private boolean isDuplicated(SourceObserver sourceObserver) {
        return sourceObservers.stream().anyMatch(sourceObserver::equals);
    }
}
