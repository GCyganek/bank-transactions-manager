package IOC;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import model.BankTransaction;
import model.TransactionsManager;
import model.util.ModelUtil;
import repository.dao.BankStatementDao;
import repository.dao.BankTransactionDao;
import repository.dao.PgBankStatementDao;
import repository.dao.PgBankTransactionDao;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Comparator;

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

    @Provides
    Loader provideLoader() { return new LocalFSLoader();}

    @Provides
    @Named("transactionComparator")
    Comparator<BankTransaction> provideTransactionComparator() {
        return ModelUtil.getDateComparator();
    }
}
