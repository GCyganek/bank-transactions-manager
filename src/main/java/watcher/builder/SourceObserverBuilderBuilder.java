package watcher.builder;

import model.util.BankType;
import model.util.SourceType;
import watcher.SourceObserver;
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


public class SourceObserverBuilderBuilder implements SourceTypeStepBuilder, BankTypeStepBuilder,
        DescriptionStepBuilder, OptionalStepBuilder
{
    private final static LocalDateTime INITIAL_FETCH_UPDATES_AFTER =
            LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

    private SourceType sourceType;
    private String description;
    private BankType bankType;
    private LocalDateTime lastUpdateTime;
    private boolean isActive;

    private SourceObserverBuilderBuilder() {
        lastUpdateTime = INITIAL_FETCH_UPDATES_AFTER;
        isActive = true;
    }

    public static SourceTypeStepBuilder with() {
        return new SourceObserverBuilderBuilder();
    }

    @Override
    public BankTypeStepBuilder withSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    @Override
    public DescriptionStepBuilder withBankType(BankType bankType) {
        this.bankType = bankType;
        return this;
    }

    @Override
    public OptionalStepBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public OptionalStepBuilder withLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    @Override
    public OptionalStepBuilder withActiveSetTo(boolean active) {
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
