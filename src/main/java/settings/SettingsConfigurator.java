package settings;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import watcher.SourceObserver;
import watcher.builder.SourceObserverBuilder;
import watcher.exceptions.InvalidSourceConfigException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class SettingsConfigurator {
    private static final Logger LOGGER = LogManager.getLogger(SettingsConfigurator.class);

    private final SettingsFactory settingsFactory;
    private SettingsConfig settingsConfig;

    @Inject
    public SettingsConfigurator(SettingsFactory settingsFactory) {
        this.settingsFactory = settingsFactory;
        loadSettings();
    }

    private void loadSettings() {
        settingsConfig = settingsFactory.createSettingsConfig();

        try {
            settingsConfig = settingsFactory.createSettingsParser().loadSettingsConfig();
        } catch (IOException exception) {
            LOGGER.warn("Failed to load config, using defaults.", exception);
        } catch (Exception exception) {
            LOGGER.warn("Config file is likely corrupted, using defaults.", exception);
        }
    }


    public List<SourceObserver> getStoredSources() {
        List<SourceObserver> sourceObservers = new LinkedList<>();

        for (var sourceConfig: settingsConfig.getSourceConfigs()) {
            try {
               SourceObserver sourceObserver = SourceObserverBuilder.with()
                        .withSourceType(sourceConfig.getSourceType())
                        .withBankType(sourceConfig.getBankType())
                        .withDescription(sourceConfig.getDescription())
                        .withLastUpdateTime(sourceConfig.getLastUpdateTime())
                        .withActiveSetTo(sourceConfig.isActive())
                        .build();

               sourceObservers.add(sourceObserver);
            } catch (InvalidSourceConfigException e) {
                // nothing to do
                LOGGER.debug("Source config for " + sourceConfig.getDescription() + " was invalid, ignoring", e);
            }
        }

        return sourceObservers;
    }

    public boolean getAutoImportStatus() {
        return settingsConfig.getAutoImportState();
    }

    public void listenForSourcesExistenceChange(ObservableList<SourceObserver> sourceObservers) {
        sourceObservers.addListener(new ListChangeListener<SourceObserver>() {
            @Override
            public void onChanged(Change<? extends SourceObserver> change) {
                boolean updateRequired = false;

                while (change.next()) {
                    if (change.wasAdded() || change.wasRemoved()) {
                        updateRequired = true;
                    }
                }

                if (updateRequired)
                    updateSourcesConfig(sourceObservers);
            }
        });
    }


    public void updateSourcesConfig(List<SourceObserver> sourceObservers) {
        settingsConfig = settingsFactory.getUpdatedSettingsConfig(this.settingsConfig, sourceObservers);
        saveSettings();
    }

    public void setAutoImportStatus(Boolean newValue) {
        settingsConfig.setAutoImportState(newValue);
        saveSettings(); // TODO lazy saving would be more efficient
    }

    private void saveSettings() {
        Observable
                .just(settingsFactory.createSettingsParser())
                .subscribeOn(Schedulers.newThread())
                .subscribe(settingsParser -> {
                    // prevent updates overwriting each other
                    synchronized (this) {
                        settingsParser.saveSettingsConfig(settingsConfig);
                    }
                }, err -> {
                    // user can't really do anything if this fails
                    LOGGER.error("Failed to update config", err);
                });
    }
}
