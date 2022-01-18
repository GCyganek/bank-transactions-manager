package watcher.directory;

import io.reactivex.rxjava3.core.Observable;
import model.util.BankType;
import model.util.DocumentType;
import watcher.AbstractSourceObserver;
import watcher.SourceType;
import watcher.SourceUpdate;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class DirectoryObserver extends AbstractSourceObserver {
    private final Path path;
    private final WatchService watchService;

    public DirectoryObserver(Path path, BankType bankType,
                             LocalDateTime lastUpdateTime, boolean isActive) throws IOException {
        super(path.toString(), bankType, SourceType.DIRECTORY, lastUpdateTime, isActive);
        this.path = path;

        this.watchService = initializeWatchService();

        // TODO at init we should check if something new was added since lastUpdateTime, then use polling
    }

    private WatchService initializeWatchService() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        return watchService;
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        return Observable.create(emitter -> {
            WatchKey key;
            while ((key = watchService.poll()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path relativeFilePath = (Path) event.context();
                    String absoluteFilePathString = path + "/" + relativeFilePath.toString();
                    getDocumentType(absoluteFilePathString)
                        .ifPresent(documentType -> emitter
                            .onNext(new DirectorySourceUpdate(this, documentType, absoluteFilePathString)));

                }
                key.reset();
            }

            lastUpdateTimeProperty().setValue(LocalDateTime.now());
            emitter.onComplete();
        });
    }

    private Optional<DocumentType> getDocumentType(String path) {
        return Optional.ofNullable(path)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(path.lastIndexOf(".") + 1))
                .flatMap(DocumentType::fromString);
    }
}
