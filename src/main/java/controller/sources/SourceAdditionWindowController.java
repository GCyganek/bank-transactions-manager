package controller.sources;

import watcher.SourceObserver;
import watcher.exceptions.InvalidSourceConfigException;

import java.io.IOException;
import java.util.Optional;

public interface SourceAdditionWindowController {
    Optional<SourceObserver> getAddedSourceObserver() throws InvalidSourceConfigException;
}
