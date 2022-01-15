package importer.loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class ByteLoader implements Loader {
    private final byte[] bytes;
    private final String source;

    public ByteLoader(byte[] bytes, String source) {
        this.bytes = bytes;
        this.source = source;
    }

    @Override
    public String getDescription() {
        return source;
    }

    @Override
    public Reader load() throws IOException {
        return new InputStreamReader(new ByteArrayInputStream(bytes));
    }
}