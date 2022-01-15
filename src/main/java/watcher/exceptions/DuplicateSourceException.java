package watcher.exceptions;

public class DuplicateSourceException extends Exception {
    public DuplicateSourceException(String uri) {
        super("Source with the same uri has been already added: " + uri);
    }
}
