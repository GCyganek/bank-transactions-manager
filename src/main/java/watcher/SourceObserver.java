package watcher;

import io.reactivex.rxjava3.core.Observable;
import javafx.beans.property.ObjectProperty;
import model.util.BankType;
import model.util.SourceType;

import java.time.LocalDateTime;

public interface SourceObserver {
    // since updates are cached we need to keep track of two time points
    // that is time at which we checked for update and time at which we
    // fetched this update
    Observable<SourceUpdate> getChanges();
    void changeImported(SourceUpdate sourceUpdate);

    String getDescription();
    BankType getBankType();
    SourceType getSourceType();

    ObjectProperty<Boolean> activeProperty();
    ObjectProperty<LocalDateTime> lastUpdateTimeProperty();
    Observable<Throwable> getSourceFailedObservable();

    void setActive(boolean active);
    boolean isActive();
}