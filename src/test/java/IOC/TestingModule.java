package IOC;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import repository.dao.BankStatementDao;
import repository.dao.BankTransactionDao;
import repository.dao.PgBankStatementDao;
import repository.dao.PgBankTransactionDao;

import javax.inject.Singleton;

public class TestingModule extends AbstractModule {

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

}
