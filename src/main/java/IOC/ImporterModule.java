package IOC;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import configurator.BankConfiguratorFactory;
import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import javafx.stage.Stage;
import repository.dao.BankStatementDao;
import repository.dao.BankTransactionDao;
import repository.dao.PgBankStatementDao;
import repository.dao.PgBankTransactionDao;

import javax.inject.Named;
import javax.inject.Singleton;

public class ImporterModule extends AbstractModule {

    private Stage primaryStage;

    public ImporterModule(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Provides
    @Singleton
    BankConfiguratorFactory provideBankConfiguratorFactory() {
        return new BankConfiguratorFactory();
    }

    @Provides
    Loader provideDefaultLoader() {
        return new LocalFSLoader();
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
}
