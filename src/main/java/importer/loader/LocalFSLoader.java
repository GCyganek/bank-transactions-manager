package importer.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class LocalFSLoader implements Loader {
    @Override
    public Reader load(String uri) throws IOException {
        return new BufferedReader(new FileReader(uri));
    }
}
