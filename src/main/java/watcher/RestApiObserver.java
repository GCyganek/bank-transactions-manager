package watcher;

import io.reactivex.rxjava3.core.Observable;

import java.net.URL;
import java.nio.file.Path;

public class RestApiObserver implements SourceObserver {
    private final URL remoteUrl;

    public RestApiObserver(URL remoteUrl) {
        this.remoteUrl = remoteUrl;
        initialize();
    }

    private void initialize() {
        // podpięcie do REST API remoteUrlem
    }

    @Override
    public Observable<Path> getChanges() {
        // przy uzyciu RestApiClient pobieramy sprawdzamy czy jest cos nowego, jesli jest to pobieramy pliki
        // i zapisujemy je w jakims folderze i zwracamy jego Path na doOnNext() w tworzonym Observable<Path>
    }

}
