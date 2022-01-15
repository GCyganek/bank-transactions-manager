package watcher;

import importer.loader.Loader;

public class RESTSourceUpdate implements SourceUpdate{
    private final String uri;
    private final int statementId;
    private RestApiClient client;

    public RESTSourceUpdate(String uri, int statementId) {
        this.uri = uri;
        this.statementId = statementId;
    }

    @Override
    public Loader executeUpdate() {
        jakies_dane = client.getStatement(statementId);
        return stworz_loadera_z_danych(jakies_dane);
    }
}
