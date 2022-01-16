package settings;

import controller.sources.TransactionSourcesViewController;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import watcher.SourceObserver;
import watcher.SourcesSupervisor;
import watcher.builder.SourceObserverBuilder;
import watcher.exceptions.InvalidSourceConfigException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class SettingsConfigurator {
    private final SourcesSupervisor sourcesSupervisor;
    private final SettingsFactory settingsFactory;
    private final ObservableList<SourceObserver> sourceObservers;

    @Inject
    public SettingsConfigurator(SettingsFactory settingsFactory,
                                SourcesSupervisor sourcesSupervisor,
                                TransactionSourcesViewController transactionSourcesViewController)
    {
        this.settingsFactory = settingsFactory;
        this.sourcesSupervisor = sourcesSupervisor;
        this.sourceObservers = transactionSourcesViewController.getSourceObservers();
    }


    public void loadSettings() {
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

        configureSources(settingsConfig);
    }

    private void setupListenningForSourceChanges() {
        sourceObservers.addListener(new ListChangeListener<SourceObserver>() {
            @Override
            public void onChanged(Change<? extends SourceObserver> change) {
                boolean updateRequired = false;

                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(sourceObserver -> bindSourceChanges(sourceObserver));
                        updateRequired = true;
                    }
                    else if (change.wasRemoved()) {
                        updateRequired = true;
                    }
                }

                if (updateRequired)
                    updateConfig();
            }
        });
    }

    private void configureSources(SettingsConfig config) {
        config.getSourceConfigs().forEach(sourceConfig -> {
            try {
                SourceObserver sourceObserver = SourceObserverBuilder.with()
                        .withSourceType(sourceConfig.getSourceType())
                        .withBankType(sourceConfig.getBankType())
                        .withDescription(sourceConfig.getDescription())
                        .withLastUpdateTime(sourceConfig.getLastUpdateTime())
                        .withActiveSetTo(sourceConfig.isActive())
                        .build();

                sourceObservers.add(sourceObserver);
                sourcesSupervisor.addSourceObserver(sourceObserver);
                bindSourceChanges(sourceObserver);
            } catch (InvalidSourceConfigException e) {
                // TODO remove 'dead' source or display as inactive
                e.printStackTrace();
            }
        });

        setupListenningForSourceChanges();
    }

    private void bindSourceChanges(SourceObserver sourceObserver) {
        // TODO updating once change is detected is very inefficient, implement caching

        sourceObserver.activeProperty().addListener(x -> updateConfig());
        sourceObserver.lastUpdateTimeProperty().addListener(x -> updateConfig());
    }

    private void updateConfig() {
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
