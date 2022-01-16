package settings.json;

import com.google.gson.annotations.SerializedName;
import settings.SettingsConfig;
import settings.SourceConfig;

import java.util.LinkedList;
import java.util.List;

public class JsonSettingsConfig implements SettingsConfig {
    @SerializedName("sources")
    private List<JsonSourceConfig> serializableSourceConfigs;

    public JsonSettingsConfig() {
        this(new LinkedList<>());
    }
    
    public JsonSettingsConfig(List<JsonSourceConfig> jsonSourceConfigs) {
        serializableSourceConfigs = jsonSourceConfigs;
    }

    @Override
    public List<SourceConfig> getSourceConfigs() {
        return new LinkedList<>(serializableSourceConfigs);
    }
}
