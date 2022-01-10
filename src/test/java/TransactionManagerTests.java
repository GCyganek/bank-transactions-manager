import IOC.TestingModule;
import com.google.inject.*;
import model.Account;
import model.BankStatement;
import model.BankTransaction;
import model.TransactionsSupervisor;
import model.util.ImportSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.BankStatementsRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;


public class TransactionManagerTests {
    private static final Injector injector = Guice.createInjector(new TestingModule());

    private TransactionsSupervisor manager;
    private Account account;

    @BeforeEach
    public void initManager() {
        BankStatementsRepository repository = mock(BankStatementsRepository.class);
        account = new Account(repository);
        manager = new TransactionsSupervisor(repository, account);
    }

    @Test
    public void uniqueTransactionCanBeAdded() {
        // given
        BankTransaction bankTransaction = getTestTransaction();

        // when
        ImportSession importSession = manager.startImportSession();
        boolean isAdded = manager.tryToAddTransaction(importSession, bankTransaction);

        // then
        assertTrue(isAdded);
    }

    @Test
    public void sameTransactionCantBeAddedTwice() {
        // given
        BankTransaction bankTransaction = getTestTransaction();

        // when
        ImportSession importSession = manager.startImportSession();
        manager.tryToAddTransaction(importSession, bankTransaction);
        boolean isAdded = manager.tryToAddTransaction(importSession, bankTransaction);

        // then
        assertFalse(isAdded);
    }

    @Test
    public void multipleTransactionsWithAtLeastOneDifferentFieldCanBeAdded() {
        // given
        List<BankTransaction> transactions = getTestTransactions();

        // when
        ImportSession importSession = manager.startImportSession();
        transactions.forEach(transaction -> manager.tryToAddTransaction(importSession, transaction));
        Set<BankTransaction> addedTransactions = manager.getTransactions();

        // then
        assertEquals(3, addedTransactions.size());
    }


    @Test
    public void transactionsAreNotImplicitlyAddedToView() {
        // given
        BankTransaction transaction = getTestTransaction();

        // when
        ImportSession importSession = manager.startImportSession();
        manager.tryToAddTransaction(importSession, transaction);

        List<BankTransaction> addedTransactions = account.getTransactionObservableList();

        // then
        assertEquals(0, addedTransactions.size());
    }


    @Test
    public void transactionsCanBeAddedToView() {
        // given
        BankTransaction transaction = getTestTransaction();

        // when
        ImportSession importSession = manager.startImportSession();
        manager.tryToAddTransaction(importSession, transaction);
        account.addTransaction(transaction);

        List<BankTransaction> addedTransactions = account.getTransactionObservableList();

        // then
        assertEquals(1, addedTransactions.size());
    }

    @Test
    public void balanceIsUpdatedAfterTransactionIsAddedToView() {
        // given
        BankTransaction transaction = getTestTransaction();

        // when
        ImportSession importSession = manager.startImportSession();
        manager.tryToAddTransaction(importSession, transaction);
        account.addTransaction(transaction);

        // then
        assertEquals(transaction.getAmount(), account.balanceProperty().getValue());
    }


    @Test
    public void balanceIsNotUpdatedBeforeTransactionIsAddedToView() {
        // given
        BankTransaction transaction = getTestTransaction();

        // when
        ImportSession importSession = manager.startImportSession();
        manager.tryToAddTransaction(importSession, transaction);

        // then
        assertEquals(BigDecimal.ZERO, account.balanceProperty().getValue());
    }


    @Test
    public void importCanBeReversedBeforeCompletion() {
        // given
        List<BankTransaction> transactions = getTestTransactions();
        ImportSession importSession = manager.startImportSession();
        transactions.forEach(transaction -> manager.tryToAddTransaction(importSession, transaction));
        transactions.forEach(transaction -> account.addTransaction(transaction));

        // when
        manager.reverseImport(importSession);

        Set<BankTransaction> addedTransactions = manager.getTransactions();
        List<BankTransaction> addedTransactionsView = account.getTransactionObservableList();
        BigDecimal balance = account.balanceProperty().getValue();

        // then
        assertEquals(0, addedTransactions.size());
        assertEquals(0, addedTransactionsView.size());
        assertEquals(BigDecimal.ZERO, balance);
    }


    @Test
    public void transactionCanUpdated() {
        // given
        BankTransaction transaction = getTestTransaction();
        ImportSession importSession = manager.startImportSession();
        manager.tryToAddTransaction(importSession, transaction);
        account.addTransaction(transaction);

        BankTransaction editedTransaction = transaction.shallowCopy();
        editedTransaction.setDescription(transaction.getDescription() + "different");

        // when
        manager.updateTransaction(transaction, editedTransaction);
        List<BankTransaction> transactionsInView = account.getTransactionObservableList();
        Set<BankTransaction> transactions = manager.getTransactions();

        // then
        assertEquals(1, transactionsInView.size());
        assertEquals(editedTransaction, transactionsInView.get(0));
        assertEquals(1, transactions.size());
        assertEquals(editedTransaction, transactions.iterator().next());
    }

    @Test
    public void sameTransactionCanBeAddedAgainIfOriginalWasUpdated() {
        // given
        BankTransaction transaction = getTestTransaction();
        ImportSession importSession = manager.startImportSession();
        manager.tryToAddTransaction(importSession, transaction);
        account.addTransaction(transaction);

        BankTransaction copy = transaction.shallowCopy();

        BankTransaction editedTransaction = transaction.shallowCopy();
        editedTransaction.setDescription(editedTransaction.getDescription() + "different");
        manager.completeImport(importSession);

        // when
        manager.updateTransaction(transaction, editedTransaction);
        ImportSession nextImportSession = manager.startImportSession();
        boolean isAdded = manager.tryToAddTransaction(nextImportSession, copy);

        // then
        assertTrue(isAdded);
    }

    private BankTransaction getTestTransaction() {
       BankTransaction transaction = new BankTransaction("abc", BigDecimal.valueOf(123), LocalDate.now());
       transaction.setBankStatement(getMockStatement());
       return transaction;
    }

    private BankStatement getMockStatement() {
        BankStatement statement = mock(BankStatement.class);
        when(statement.getPeriodStartDate()).thenReturn(LocalDate.now());
        when(statement.getPeriodEndDate()).thenReturn(LocalDate.now());
        return statement;
    }


    private List<BankTransaction> getTestTransactions() {
        BankTransaction bankTransaction1 = getTestTransaction();
        BankTransaction bankTransaction2 = new BankTransaction("bcd", BigDecimal.valueOf(123), LocalDate.now());
        BankTransaction bankTransaction3 = new BankTransaction("bcd", BigDecimal.valueOf(777), LocalDate.now());
        BankStatement statement = getMockStatement();

        List<BankTransaction> result = List.of(bankTransaction1, bankTransaction2, bankTransaction3);
        result.forEach(transaction -> transaction.setBankStatement(statement));

        return result;
    }
}
