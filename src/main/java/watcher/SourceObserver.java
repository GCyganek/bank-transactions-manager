package watcher;

import io.reactivex.rxjava3.core.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import model.util.BankType;

import java.time.LocalDateTime;

public interface SourceObserver {

    Observable<SourceUpdate> getChanges();

    StringProperty descriptionProperty();

    ObjectProperty<BankType> bankTypeProperty();

    ObjectProperty<Boolean> activeProperty();

    ObjectProperty<LocalDateTime> lastUpdateTimeProperty();

    Observable<Throwable> getSourceFailedObservable();

    void setActive(boolean active);

    SourceType getSourceType();
}