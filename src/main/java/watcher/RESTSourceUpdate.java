package watcher;

import importer.loader.Loader;
import io.reactivex.rxjava3.core.Observable;

public class RESTSourceUpdate implements SourceUpdate{
    private final String uri;
    private final int statementId;
    private RestApiClient client;

    public RESTSourceUpdate(String uri, int statementId) {
        this.uri = uri;
        this.statementId = statementId;
    }

    @Override
    public Observable<Loader> executeUpdate() {
        return client.getStatement(statementId);
    }
}
