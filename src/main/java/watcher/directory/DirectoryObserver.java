package watcher.directory;

import io.reactivex.rxjava3.core.Observable;
import model.util.BankType;
import model.util.DocumentType;
import model.util.SourceType;
import watcher.AbstractSourceObserver;
import watcher.SourceUpdate;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class DirectoryObserver extends AbstractSourceObserver {
    private final Path path;
    private final WatchService watchService;
    private boolean firstCheck;
    private final LocalDateTime initializationTime;

    public DirectoryObserver(Path path, BankType bankType,
                             LocalDateTime lastUpdateCheckTime, boolean isActive) throws IOException {
        super(path.toString(), bankType, SourceType.DIRECTORY, lastUpdateCheckTime, isActive);
        this.path = path;
        this.firstCheck = true;

        this.initializationTime = LocalDateTime.now();
        this.watchService = initializeWatchService();
    }

    private WatchService initializeWatchService() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        return watchService;
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        return Observable.create(emitter -> {
            try {
                lastUpdateCheckTime = LocalDateTime.now();

                if (firstCheck) {
                    firstCheck = false;
                    for (File file : handleFirstCheck()) {
                        if (emitter.isDisposed()) break;
                        fileToSourceUpdate(file.getAbsolutePath()).ifPresent(emitter::onNext);
                    }
                }

                WatchKey key;
                while (!emitter.isDisposed() && (key = watchService.poll()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (emitter.isDisposed()) {
                            key.reset();
                            break;
                        }
                        Path relativeFilePath = (Path) event.context();
                        String absoluteFilePathString = path + "/" + relativeFilePath.toString();
                        fileToSourceUpdate(absoluteFilePathString).ifPresent(emitter::onNext);
                    }
                    key.reset();
                }

                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
                sourceFailedPublisher.onNext(e);
            }
        });
    }

    // working properly only on windows, linux doesn't save a file creation time -> for example file copied from another
    // folder that was last modified before lastUpdateTimeProperty value will be ignored here on linux
    private File[] handleFirstCheck() {
        File observedDirectory = new File(path.toUri());
        return observedDirectory.listFiles(file -> {
            try {
                BasicFileAttributes fileAttr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                FileTime creationTime = fileAttr.creationTime();
                LocalDateTime convertedCreationTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                return convertedCreationTime.isAfter(lastUpdateTimeProperty().get())
                        && convertedCreationTime.isBefore(initializationTime);
            } catch (IOException e) {
                sourceFailedPublisher.onNext(e);
                e.printStackTrace();
                return false;
            }
        });
    }

    private Optional<SourceUpdate> fileToSourceUpdate(String absoluteFilePathString) {
        Optional<DocumentType> documentType = getDocumentType(absoluteFilePathString);
        return documentType.map(type ->
                new DirectorySourceUpdate(this, type, absoluteFilePathString, lastUpdateCheckTime)
        );
    }

    private Optional<DocumentType> getDocumentType(String path) {
        return Optional.ofNullable(path)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(path.lastIndexOf(".") + 1))
                .flatMap(DocumentType::fromString);
    }
}
