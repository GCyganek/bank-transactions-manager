package watcher;

import io.reactivex.rxjava3.core.Observable;
import javafx.beans.property.ObjectProperty;
import model.util.BankType;

import java.time.LocalDateTime;

public interface SourceObserver {
    Observable<SourceUpdate> getChanges();

    String getDescription();
    BankType getBankType();
    SourceType getSourceType();

    ObjectProperty<Boolean> activeProperty();
    ObjectProperty<LocalDateTime> lastUpdateTimeProperty();
    Observable<Throwable> getSourceFailedObservable();

    void setActive(boolean active);
    boolean isActive();
}