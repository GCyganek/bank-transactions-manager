package settings;

import java.util.List;

public interface SettingsConfig {
    List<SourceConfig> getSourceConfigs();
    boolean getAutoImportState();

    void setAutoImportState(boolean autoImportState);
}
