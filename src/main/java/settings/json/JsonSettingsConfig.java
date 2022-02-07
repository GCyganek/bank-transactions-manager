package settings.json;

import com.google.gson.annotations.SerializedName;
import settings.SettingsConfig;
import settings.SourceConfig;

import java.util.LinkedList;
import java.util.List;

public class JsonSettingsConfig implements SettingsConfig {
    @SerializedName("sources")
    private List<JsonSourceConfig> serializableSourceConfigs;

    @SerializedName("auto_import")
    private Boolean autoImport;

    public JsonSettingsConfig() {
        this(new LinkedList<>(), false);
    }
    
    public JsonSettingsConfig(List<JsonSourceConfig> jsonSourceConfigs, boolean autoImport) {
        serializableSourceConfigs = jsonSourceConfigs;
        this.autoImport = autoImport;
    }

    @Override
    public List<SourceConfig> getSourceConfigs() {
        return new LinkedList<>(serializableSourceConfigs);
    }

    @Override
    public boolean getAutoImportState() {
        return autoImport;
    }

    @Override
    public void setAutoImportState(boolean autoImport) {
        this.autoImport = autoImport;
    }
}
