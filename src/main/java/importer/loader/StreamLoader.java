package importer.loader;

import java.io.*;

public class StreamLoader implements Loader {
    private final InputStream stream;
    private final String source;

    public StreamLoader(InputStream stream, String sourceDescription) {
        this.stream = stream;
        this.source = sourceDescription;
    }

    @Override
    public String getDescription() {
        return source;
    }

    @Override
    public Reader load() throws IOException {
        return new InputStreamReader(stream);
    }
}