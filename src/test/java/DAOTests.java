import IOC.TestingModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import model.BankStatement;
import model.BankTransaction;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.BankStatementsRepository;
import repository.dao.BankStatementDao;
import repository.dao.BankTransactionDao;
import session.HibernateSessionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DAOTests {

    private static final Injector injector = Guice.createInjector(new TestingModule());

    private final BankStatementDao bankStatementDao = injector.getInstance(BankStatementDao.class);

    private final BankTransactionDao bankTransactionDao = injector.getInstance(BankTransactionDao.class);

    private final BankTransaction bankTransactionExample1 =
            new BankTransaction("Przelew 1", new BigDecimal("2512.23"),
                    LocalDate.of(2021, 11, 22));

    private final BankTransaction bankTransactionExample2 =
            new BankTransaction("Przelew 2", new BigDecimal("23.23"),
                    LocalDate.of(2021, 11, 23));

    private final BankStatement bankStatementExample1 =
            new BankStatement("1234 1234 1234 1234", LocalDate.of(2021, 11, 21),
                    LocalDate.of(2021, 11, 25), new BigDecimal("250.22"),
                    new BigDecimal("302.10"), "Jan Kowalski", "PLN");

    private final BankStatement bankStatementExample2 =
            new BankStatement("1234 4321 4312 1234", LocalDate.of(2021, 11, 20),
                    LocalDate.of(2021, 11, 26), new BigDecimal("2453.22"),
                    new BigDecimal("332.10"), "Anna Kowalska", "PLN");

    @BeforeEach
    public void before() {
        HibernateSessionService.openSession();
    }

    @AfterEach
    public void after() {
        HibernateSessionService.closeSession();
    }

    @AfterAll
    public static void afterAll() {
        BankStatementsRepository bankStatementsRepository = injector.getInstance(BankStatementsRepository.class);
        bankStatementsRepository.removeAllStatements();

        HibernateSessionService.openSession();
        Session session = HibernateSessionService.getSession();
        Transaction transaction = session.getTransaction();
        transaction.begin();
        HibernateSessionService.getSession().createQuery("DELETE FROM BankTransaction").executeUpdate();
        transaction.commit();
        HibernateSessionService.closeSession();
    }

    @Test
    public void createTwoBankStatementsTest() {
        // When
        var bankStatement1 = bankStatementDao.create(bankStatementExample1);
        var bankStatement2 = bankStatementDao.create(bankStatementExample2);

        // Then
        checkBankStatement(bankStatementDao.findById(bankStatement1.getId()));
        checkBankStatement(bankStatementDao.findById(bankStatement2.getId()));

        assertNotEquals(bankStatement1.getId(), bankStatement2.getId());
    }

    @Test
    public void createTwoBankTransactionsTest() {
        // When
        var bankTransaction1 = bankTransactionDao.create(bankTransactionExample1);
        var bankTransaction2 = bankTransactionDao.create(bankTransactionExample2);

        // Then
        checkBankTransaction(bankTransactionDao.findById(bankTransaction1.getId()));
        checkBankTransaction(bankTransactionDao.findById(bankTransaction2.getId()));

        assertNotEquals(bankTransaction1.getId(), bankTransaction2.getId());
    }

    @Test
    public void addBankTransactionToBankStatementTest() {
        // When
        var bankTransaction = bankTransactionDao.create(bankTransactionExample1);
        var bankStatement = bankStatementDao.create(bankStatementExample1);
        // Then
        checkBankTransaction(bankTransactionDao.findById(bankTransaction.getId()));
        checkBankStatement(bankStatementDao.findById(bankStatement.getId()));

        bankStatementDao.addTransaction(bankStatement, bankTransaction);

        var bankTransactionSet = bankStatement.getBankTransactionSet();
        assertEquals(bankTransactionSet.size(), 1);
        assertTrue(bankTransactionSet.contains(bankTransaction));
        assertEquals(bankTransaction.getBankStatement(), bankStatement);
    }

    @Test
    public void addTwoBankTransactionsToBankStatementWithCascadeTest() {
        // When
        var bankTransaction1 = bankTransactionDao.create(bankTransactionExample1);
        var bankTransaction2 = bankTransactionDao.create(bankTransactionExample2);
        var bankStatement = bankStatementDao.create(bankStatementExample1);

        // Then
        checkBankTransaction(bankTransactionDao.findById(bankTransaction1.getId()));
        checkBankTransaction(bankTransactionDao.findById(bankTransaction2.getId()));
        checkBankStatement(bankStatementDao.findById(bankStatement.getId()));

        bankStatementDao.addTransaction(bankStatement, bankTransaction1);
        bankStatementDao.addTransaction(bankStatement, bankTransaction2);

        var bankTransactionSet = bankStatement.getBankTransactionSet();
        assertEquals(bankTransactionSet.size(), 2);
        assertTrue(bankTransactionSet.contains(bankTransaction1));
        assertTrue(bankTransactionSet.contains(bankTransaction2));
        assertEquals(bankTransaction1.getBankStatement(), bankStatement);
        assertEquals(bankTransaction2.getBankStatement(), bankStatement);

        BankStatementsRepository bankStatementsRepository = injector.getInstance(BankStatementsRepository.class);
        bankStatementsRepository.getAllStatements();
    }

    private void checkBankStatement(final Optional<BankStatement> bankStatement) {
        assertTrue(bankStatement.isPresent());
        bankStatement.ifPresent(bs -> {
            assertTrue(bs.getId() > 0);
            assertNotNull(bs.getAccountNumber());
            assertNotNull(bs.getAccountOwner());
            assertNotNull(bs.getPaidIn());
            assertNotNull(bs.getPaidOut());
            assertNotNull(bs.getPeriodStartDate());
            assertNotNull(bs.getPeriodEndDate());
            assertNotNull(bs.getCurrency());
        });
    }

    private void checkBankTransaction(final Optional<BankTransaction> bankTransaction) {
        assertTrue(bankTransaction.isPresent());
        bankTransaction.ifPresent(bt -> {
            assertTrue(bt.getId() > 0);
            assertNotNull(bt.getAmount());
            assertNotNull(bt.getDate());
            assertNotNull(bt.getDescription());
        });
    }
}
