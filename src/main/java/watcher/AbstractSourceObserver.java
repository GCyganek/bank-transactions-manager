package watcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.util.BankType;

import java.time.LocalDateTime;

public abstract class AbstractSourceObserver implements SourceObserver {
    protected final StringProperty descriptionProperty = new SimpleStringProperty();
    protected final ObjectProperty<BankType> bankType = new SimpleObjectProperty<>();
    protected final ObjectProperty<Boolean> active = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDateTime> lastUpdateTimeProperty = new SimpleObjectProperty<>();
    protected final PublishSubject<Throwable> sourceFailedPublisher;
    protected final SourceType sourceType;

    public AbstractSourceObserver(String description, BankType bankType, SourceType sourceType,
                                  LocalDateTime lastUpdateTime, boolean isActive)
    {
        this.descriptionProperty.setValue(description);
        this.bankType.setValue(bankType);
        this.active.setValue(isActive);
        this.lastUpdateTimeProperty.setValue(lastUpdateTime);
        this.sourceFailedPublisher = PublishSubject.create();
        this.sourceType = sourceType;
    }


    @Override
    public StringProperty descriptionProperty() {
        return descriptionProperty;
    }

    @Override
    public ObjectProperty<BankType> bankTypeProperty() {
        return bankType;
    }

    @Override
    public ObjectProperty<Boolean> activeProperty() {
        return active;
    }

    @Override
    public Observable<Throwable> getSourceFailedObservable() {
        return Observable.wrap(sourceFailedPublisher);
    }

    @Override
    public void setActive(boolean active) {
        this.active.setValue(active);
    }

    @Override
    public SourceType getSourceType() {
        return sourceType;
    }

    @Override
    public ObjectProperty<LocalDateTime> lastUpdateTimeProperty() {
        return lastUpdateTimeProperty;
    }
}
