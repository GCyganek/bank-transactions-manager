package watcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.util.BankType;
import model.util.SourceType;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class AbstractSourceObserver implements SourceObserver {
    protected final String description;
    protected final BankType bankType;
    protected final SourceType sourceType;

    protected final ObjectProperty<Boolean> active = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDateTime> lastUpdateTimeProperty = new SimpleObjectProperty<>();
    protected final PublishSubject<Throwable> sourceFailedPublisher;

    protected LocalDateTime lastUpdateCheckTime;

    public AbstractSourceObserver(String description, BankType bankType, SourceType sourceType,
                                  LocalDateTime lastUpdateTime, boolean isActive)
    {
        this.description = description;
        this.bankType = bankType;
        this.sourceType = sourceType;
        this.lastUpdateCheckTime = lastUpdateTime;

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

        if (!active) {
            lastUpdateCheckTime = lastUpdateTimeProperty.get();
        }
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSourceObserver that = (AbstractSourceObserver) o;
        return Objects.equals(description, that.description) && Objects.equals(bankType, that.bankType) &&
                Objects.equals(sourceType, that.sourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, bankType, sourceType);
    }

    @Override
    public void changeImported(SourceUpdate sourceUpdate) {
        if (sourceUpdate.getUpdateCheckTime().compareTo(lastUpdateTimeProperty.get()) > 0)
            lastUpdateTimeProperty.setValue(lastUpdateCheckTime);
    }
}
