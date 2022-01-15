package watcher;

import io.reactivex.rxjava3.core.Observable;
import model.util.BankType;

import java.io.IOException;
import java.nio.file.*;

public class DirectoryObserver implements SourceObserver {
    private final Path path;
    private final BankType bankType;
    private final WatchService watchService;

    public DirectoryObserver(Path path, BankType bankType) throws IOException {
        this.path = path;
        this.bankType = bankType;
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
                    Path string = (Path) event.context();
                    emitter.onNext(new DirectorySourceUpdate(bankType, path + "/" + string.toString()));
                }
                key.reset();
            }
            emitter.onComplete();
        });
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.DIRECTORY;
    }

}
