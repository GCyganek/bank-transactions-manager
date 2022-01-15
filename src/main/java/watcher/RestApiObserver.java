package watcher;

import io.reactivex.rxjava3.core.Observable;

import java.net.URL;
import java.nio.file.Path;

public class RestApiObserver implements SourceObserver {
    private final URL remoteUrl;
    private final RestApiClient client;

    public RestApiObserver(URL remoteUrl, RestApiClient client) {
        this.remoteUrl = remoteUrl;
        this.client = client;
        initialize();
    }

    private void initialize() {
        // podpięcie do REST API remoteUrlem
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        // przy uzyciu RestApiClient pobieramy sprawdzamy czy jest cos nowego, jesli jest to pobieramy pliki
        // i zapisujemy je w jakims folderze i zwracamy jego Path na doOnNext() w tworzonym Observable<Path>
        aktualizacje = client.getUpdates(remoteUrl, data_poczatu, data_konca);
        return new RESTSourceUpdate(remoteUrl, aktualizcja.statement_id);
    }

}
