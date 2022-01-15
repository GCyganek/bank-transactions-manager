package importer.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class LocalFSLoader implements Loader {
    private final String uri;

    @Override
    public String getDescription() {
        return uri;
    }

    public LocalFSLoader(String uri) {
        this.uri = uri;
    }

    @Override
    public Reader load() throws IOException {
        return new BufferedReader(new FileReader(uri));
    }
}
