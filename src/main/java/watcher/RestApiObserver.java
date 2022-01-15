package watcher;

import io.reactivex.rxjava3.core.Observable;

import java.net.URL;
import java.nio.file.Path;

public class RestApiObserver implements SourceObserver {
    private final URL remoteUrl;
    private final RestApiClient client;

    public RestApiObserver(URL remoteUrl, SourceType type) {
        this.remoteUrl = remoteUrl;
        initialize();
        data_poczatku = now or 2020
    }

    public SourceType getType()

    private void initialize() {
        // podpięcie do REST API remoteUrlem
        client = hwdp;
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        // przy uzyciu RestApiClient pobieramy sprawdzamy czy jest cos nowego, jesli jest to pobieramy pliki
        // i zapisujemy je w jakims folderze i zwracamy jego Path na doOnNext() w tworzonym Observable<Path>
        aktualizacje = client.getUpdates(remoteUrl, data_poczatu);
        return new RESTSourceUpdate(remoteUrl, aktualizcja.statement_id);
    }

}
