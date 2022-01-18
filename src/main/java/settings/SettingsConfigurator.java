package settings;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
    private final SettingsFactory settingsFactory;

    @Inject
    public SettingsConfigurator(SettingsFactory settingsFactory) {
        this.settingsFactory = settingsFactory;

    }


    public List<SourceObserver> loadSourcesSettings() {
        SettingsConfig settingsConfig;
        try {
            settingsConfig = settingsFactory.createSettingsParser().loadSettingsConfig();
        } catch (IOException exception) {
            System.out.println("Failed to load config, creating defaults. error:\n" + exception.getMessage());
            exception.printStackTrace();
            settingsConfig = settingsFactory.createSettingsConfig();
        } catch (Exception exception) {
            System.out.println("Config file is likely corrupted, using defaults. error:\n" + exception.getMessage());
            exception.printStackTrace();
            settingsConfig = settingsFactory.createSettingsConfig();
        }

        return configureSources(settingsConfig);
    }


    private List<SourceObserver> configureSources(SettingsConfig config) {
        List<SourceObserver> sourceObservers = new LinkedList<>();

        for (var sourceConfig: config.getSourceConfigs()) {
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
                System.out.println("Source config for " + sourceConfig.getDescription() + " was invalid, ignoring");
                e.printStackTrace();
            }
        }

        return sourceObservers;
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
        SettingsConfig settingsConfig = settingsFactory.createSettingsConfig(sourceObservers);
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
                    System.out.println("Failed to update config");
                    err.printStackTrace();
                });
    }
}
