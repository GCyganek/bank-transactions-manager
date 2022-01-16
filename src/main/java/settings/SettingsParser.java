package settings;

import settings.SettingsConfig;
import settings.SourceConfig;
import watcher.SourceObserver;

import java.io.IOException;

public interface SettingsParser {
    SettingsConfig loadSettingsConfig() throws IOException;
    void saveSettingsConfig(SettingsConfig settingsConfig) throws IOException;
}
