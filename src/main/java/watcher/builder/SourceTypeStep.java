package watcher.builder;

import watcher.SourceType;

public interface SourceTypeStep {
    BankTypeStep withSourceType(SourceType sourceType);
}
