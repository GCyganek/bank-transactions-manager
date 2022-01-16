package settings.json;

import settings.SettingsConfig;
import settings.SettingsFactory;
import settings.SourceConfig;
import settings.SettingsParser;
import watcher.SourceObserver;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

public class JsonSettingsFactory implements SettingsFactory {
    private static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";

    private final String configPath;

    @Inject
    public JsonSettingsFactory(@Named("config_path") String configPath) {
        this.configPath = configPath;
    }

    @Override
    public SettingsParser createSettingsParser() {
        return new JsonSettingsParser(configPath, DATE_TIME_FORMAT);
    }

    @Override
    public SourceConfig createSourceConfig(SourceObserver sourceObserver) {
        return createJsonSettingsConfig(sourceObserver);
    }

    @Override
    public SettingsConfig createSettingsConfig() {
        return new JsonSettingsConfig();
    }

    @Override
    public SettingsConfig createSettingsConfig(List<SourceObserver> sourceObservers) {
        List<JsonSourceConfig> sourceConfigs = sourceObservers.stream().map(this::createJsonSettingsConfig).toList();
        return new JsonSettingsConfig(sourceConfigs);
    }

    private JsonSourceConfig createJsonSettingsConfig(SourceObserver sourceObserver) {
        JsonSourceConfig sourceConfig = new JsonSourceConfig();

        sourceConfig.setSourceType(sourceObserver.getSourceType());
        sourceConfig.setBankType(sourceObserver.bankTypeProperty().get());
        sourceConfig.setLastUpdateTime(sourceObserver.lastUpdateTimeProperty().get());
        sourceConfig.setActive(sourceObserver.activeProperty().get());
        sourceConfig.setDescription(sourceObserver.descriptionProperty().get());

        return sourceConfig;
    }
}
