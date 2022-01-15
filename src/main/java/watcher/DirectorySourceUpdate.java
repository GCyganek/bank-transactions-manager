package watcher;

import importer.loader.Loader;
import importer.loader.LocalFSLoader;

public class DirectorySourceUpdate implements SourceUpdate{
    private String path;
    @Override
    public Loader executeUpdate() {
        return new LocalFSLoader(path);
        return null;
    }
}
