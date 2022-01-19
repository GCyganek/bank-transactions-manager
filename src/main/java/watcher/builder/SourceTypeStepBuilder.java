package watcher.builder;

import model.util.SourceType;

public interface SourceTypeStepBuilder {
    BankTypeStepBuilder withSourceType(SourceType sourceType);
}
