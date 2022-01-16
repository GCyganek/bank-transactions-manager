package watcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.util.BankType;

public abstract class AbstractSourceObserver implements SourceObserver {
    protected final StringProperty descriptionProperty = new SimpleStringProperty();
    protected final ObjectProperty<BankType> bankType = new SimpleObjectProperty<>();
    protected final PublishSubject<Throwable> sourceFailedPublisher;
    protected final SourceType sourceType;

    public AbstractSourceObserver(String description, BankType bankType, SourceType sourceType) {
        this.descriptionProperty.setValue(description);
        this.bankType.setValue(bankType);
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
    public Observable<Throwable> getSourceFailedObservable() {
        return Observable.wrap(sourceFailedPublisher);
    }

    @Override
    public SourceType getSourceType() {
        return sourceType;
    }
}
