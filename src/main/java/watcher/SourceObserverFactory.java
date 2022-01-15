package watcher;

import com.google.inject.Singleton;
import model.util.BankType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

@Singleton
public class SourceObserverFactory {

    public static SourceObserver initializeSourceObserver(BankType bankType, String sourceUri, SourceType sourceType) throws InvalidSourceConfigException, IOException {
        return switch (sourceType) {
            case REST_API -> new RestApiObserver(new URL(sourceUri), bankType);
            case DIRECTORY -> new DirectoryObserver(Path.of(sourceUri), bankType);
        };
    }

}
