package watcher.builder;

import watcher.SourceObserver;
import watcher.exceptions.InvalidSourceConfigException;

import java.time.LocalDateTime;

public interface OptionalStepBuilder {
    OptionalStepBuilder withLastUpdateTime(LocalDateTime lastUpdateTime);

    OptionalStepBuilder withActiveSetTo(boolean active);

    SourceObserver build() throws InvalidSourceConfigException;
}
