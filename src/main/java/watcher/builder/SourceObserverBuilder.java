package watcher.builder;

import model.util.BankType;
import watcher.SourceObserver;
import watcher.SourceType;
import watcher.directory.DirectoryObserver;
import watcher.exceptions.InvalidSourceConfigException;
import watcher.restapi.RestApiObserver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


public class SourceObserverBuilder implements SourceTypeStep, BankTypeStep,
        DescriptionStep, OptionalStep
{
    private final static LocalDateTime INITIAL_FETCH_UPDATES_AFTER =
            LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

    private SourceType sourceType;
    private String description;
    private BankType bankType;
    private LocalDateTime lastUpdateTime;
    private boolean isActive;

    private SourceObserverBuilder() {
        lastUpdateTime = INITIAL_FETCH_UPDATES_AFTER;
        isActive = true;
    }

    public static SourceTypeStep with() {
        return new SourceObserverBuilder();
    }

    @Override
    public BankTypeStep withSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    @Override
    public DescriptionStep withBankType(BankType bankType) {
        this.bankType = bankType;
        return this;
    }

    @Override
    public OptionalStep withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public OptionalStep withLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    @Override
    public OptionalStep withActiveSetTo(boolean active) {
        this.isActive = active;
        return this;
    }

    @Override
    public SourceObserver build() throws InvalidSourceConfigException {
        try {
            return switch (sourceType) {
                case REST_API -> new RestApiObserver(getUrl(description), bankType, lastUpdateTime, isActive);
                case DIRECTORY -> new DirectoryObserver(getPath(description), bankType, lastUpdateTime, isActive);
            };
        } catch (IOException exception) {
            throw new InvalidSourceConfigException(exception.getMessage());
        }
    }

    private URL getUrl(String description) throws InvalidSourceConfigException {
        try {
            URL url = new URL(description);
            url.toURI();
            return url;
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            throw new InvalidSourceConfigException(e.getMessage());
        }
    }

    private Path getPath(String description) throws InvalidSourceConfigException {
        File file = new File(description);

        if (!file.isDirectory()) {
            throw new InvalidSourceConfigException("Directory at " + description + " doesn't exist");
        }

        return file.toPath();
    }
}
