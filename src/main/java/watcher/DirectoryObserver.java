package watcher;

import io.reactivex.rxjava3.core.Observable;

import java.io.IOException;
import java.nio.file.*;

public class DirectoryObserver implements SourceObserver {
    private final Path path;
    private final WatchService watchService;

    public DirectoryObserver(Path path) throws IOException {
        this.path = path;
        this.watchService = FileSystems.getDefault().newWatchService();
        initialize();
    }

    private void initialize() throws IOException {
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        return Observable.create(emitter -> {
            WatchKey key;
            while ((key = watchService.poll()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path string = (Path) event.context();
                    emitter.onNext(new DirectorySourceUpdate(string));
                }
                key.reset();
            }
            emitter.onComplete();
        });
    }

    public static void main(String[] args) {
        String currentWorkingDir = System.getProperty("user.dir");
        System.out.println(currentWorkingDir);
        try {
            DirectoryObserver directoryObserver = new DirectoryObserver(Paths.get(currentWorkingDir));
            Thread.sleep(10000);
            System.out.println("waking up...");
            directoryObserver.getChanges().doOnNext(System.out::println).subscribe();
            Thread.sleep(3000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
