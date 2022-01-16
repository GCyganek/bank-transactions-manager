package settings;

import watcher.SourceObserver;

import java.util.List;

public interface SettingsFactory {
    SettingsParser createSettingsParser();
    SourceConfig createSourceConfig(SourceObserver sourceObserver);
    SettingsConfig createSettingsConfig();
    SettingsConfig createSettingsConfig(List<SourceObserver> sourceObservers);
}
