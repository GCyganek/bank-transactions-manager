package IOC;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import configurator.BankConfiguratorFactory;
import javafx.stage.Stage;
import model.Account;
import model.TransactionStatsManager;
import repository.dao.BankStatementDao;
import repository.dao.BankTransactionDao;
import repository.dao.PgBankStatementDao;
import repository.dao.PgBankTransactionDao;
import settings.SettingsFactory;
import settings.json.JsonSettingsFactory;

import javax.inject.Named;
import javax.inject.Singleton;

public class ImporterModule extends AbstractModule {

    private Stage primaryStage;

    public ImporterModule(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    protected void configure() {
        bind(Account.class).asEagerSingleton();
        bind(TransactionStatsManager.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    BankConfiguratorFactory provideBankConfiguratorFactory() {
        return new BankConfiguratorFactory();
    }


    @Provides
    @Singleton
    BankStatementDao provideBankStatementDao() {
        return new PgBankStatementDao();
    }

    @Provides
    @Singleton
    BankTransactionDao provideBankTransactionDao() {
        return new PgBankTransactionDao();
    }

    @Provides
    @Named("primaryStage")
    Stage providePrimaryStage() {
        return this.primaryStage;
    }

    @Provides
    @Singleton
    SettingsFactory provideSettingsFactory(JsonSettingsFactory jsonSettingsFactory) {
        return jsonSettingsFactory;
    }

    @Provides
    @Named("config_path")
    String provideConfigPath() {
        return "config.json";
    }
}
