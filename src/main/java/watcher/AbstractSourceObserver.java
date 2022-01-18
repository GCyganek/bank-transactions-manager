package watcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.util.BankType;

import java.time.LocalDateTime;

public abstract class AbstractSourceObserver implements SourceObserver {
    protected final String description;
    protected final BankType bankType;
    protected final SourceType sourceType;

    protected final ObjectProperty<Boolean> active = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDateTime> lastUpdateTimeProperty = new SimpleObjectProperty<>();
    protected final PublishSubject<Throwable> sourceFailedPublisher;

    public AbstractSourceObserver(String description, BankType bankType, SourceType sourceType,
                                  LocalDateTime lastUpdateTime, boolean isActive)
    {
        this.description = description;
        this.bankType = bankType;
        this.sourceType = sourceType;

        this.active.setValue(isActive);
        this.lastUpdateTimeProperty.setValue(lastUpdateTime);
        this.sourceFailedPublisher = PublishSubject.create();
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
    public boolean isActive() {
        return this.active.get();
    }

    @Override
    public SourceType getSourceType() {
        return sourceType;
    }

    @Override
    public ObjectProperty<LocalDateTime> lastUpdateTimeProperty() {
        return lastUpdateTimeProperty;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public BankType getBankType() {
        return bankType;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (other instanceof SourceObserver that) {
            return this.sourceType == that.getSourceType() &&
                    this.bankType.equals(that.getBankType()) &&
                    this.description.equals(that.getDescription());
        }

        return false;
    }
}
