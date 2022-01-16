package watcher.builder;

import watcher.SourceObserver;
import watcher.exceptions.InvalidSourceConfigException;

import java.time.LocalDateTime;

public interface OptionalStep {
    OptionalStep withLastUpdateTime(LocalDateTime lastUpdateTime);

    OptionalStep withActiveSetTo(boolean active);

    SourceObserver build() throws InvalidSourceConfigException;
}
