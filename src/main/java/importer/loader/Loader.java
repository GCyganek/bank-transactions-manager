package importer.loader;

import java.io.IOException;
import java.io.Reader;

public interface Loader {
    Reader load() throws IOException;
    String getDescription();
}
