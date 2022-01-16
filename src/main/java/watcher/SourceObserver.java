package watcher;

import io.reactivex.rxjava3.core.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import model.util.BankType;

public interface SourceObserver {

    Observable<SourceUpdate> getChanges();

    StringProperty descriptionProperty();

    ObjectProperty<BankType> bankTypeProperty();

    ObjectProperty<Boolean> activeProperty();

    Observable<Throwable> getSourceFailedObservable();

    void setActive(boolean active);

    SourceType getSourceType();
}