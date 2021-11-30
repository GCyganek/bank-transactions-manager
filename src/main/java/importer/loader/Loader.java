package importer.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface Loader {
    Reader load(String URI) throws IOException;
}
