package IOC;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import configurator.BankConfiguratorFactory;
import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import repository.BankStatementsRepository;
import repository.PgBankStatementsRepository;
import repository.dao.BankStatementDao;
import repository.dao.BankTransactionDao;
import repository.dao.PgBankStatementDao;
import repository.dao.PgBankTransactionDao;

public class ImporterModule extends AbstractModule {

    @Provides
    BankConfiguratorFactory provideBankConfiguratorFactory() {
        return new BankConfiguratorFactory();
    }

    @Provides
    BankStatementsRepository provideBankStatementsRepository(PgBankStatementsRepository repository) {
        return repository;
    }

    @Provides
    Loader provideDefaultLoader() {
        return new LocalFSLoader();
    }

    @Provides
    BankStatementDao provideBankStatementDao() {
        return new PgBankStatementDao();
    }

    @Provides
    BankTransactionDao provideBankTransactionDao() {
        return new PgBankTransactionDao();
    }

}
