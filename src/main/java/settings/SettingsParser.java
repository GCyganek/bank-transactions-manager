package settings;

import java.io.IOException;

public interface SettingsParser {
    SettingsConfig loadSettingsConfig() throws IOException;
    void saveSettingsConfig(SettingsConfig settingsConfig) throws IOException;
}
