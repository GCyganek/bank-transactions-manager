package watcher.directory;

import io.reactivex.rxjava3.core.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.util.BankType;
import watcher.SourceObserver;
import watcher.SourceType;
import watcher.SourceUpdate;

import java.io.IOException;
import java.nio.file.*;

public class DirectoryObserver implements SourceObserver {
    private final Path path;
    private final StringProperty pathStringProperty = new SimpleStringProperty();
    private final ObjectProperty<BankType> bankType = new SimpleObjectProperty<>();
    private final WatchService watchService;

    public DirectoryObserver(Path path, BankType bankType) throws IOException {
        this.path = path;
        this.pathStringProperty.set(path.toString());
        this.bankType.set(bankType);
        this.watchService = FileSystems.getDefault().newWatchService();
        initialize();
    }

    private void initialize() throws IOException {
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        System.out.println(path);
        return Observable.create(emitter -> {
            WatchKey key;
            while ((key = watchService.poll()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path relativeFilePath = (Path) event.context();
                    String absoluteFilePathString = path + "/" + relativeFilePath.toString();
                    emitter.onNext(new DirectorySourceUpdate(bankType.get(), absoluteFilePathString));
                }
                key.reset();
            }
            emitter.onComplete();
        });
    }

    @Override
    public StringProperty descriptionProperty() { return pathStringProperty; }

    @Override
    public ObjectProperty<BankType> bankTypeProperty() { return bankType; }

}
