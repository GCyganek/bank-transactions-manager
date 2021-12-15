import IOC.TestingModule;
import com.google.inject.*;
import com.google.inject.name.Names;
import model.BankStatement;
import model.BankTransaction;
import model.TransactionsManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import repository.BankStatementsRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;




public class TransactionManagerTests {
    private static final Injector injector = Guice.createInjector(new TestingModule());

    private TransactionsManager manager;

    @BeforeEach
    public void initManager() {
        manager = new TransactionsManager(mock(BankStatementsRepository.class),
                injector.getInstance(Key.get(new TypeLiteral<Comparator<BankTransaction>>(){},
                        Names.named("transactionComparator"))));
    }

    @Test
    public void uniqueTransactionCanBeAdded() {
        // given
        BankTransaction bankTransaction = getTestTransaction();

        // when
        int sid = manager.startImportSession();
        boolean isValid = manager.isValid(sid, bankTransaction);

        // then
        assertTrue(isValid);
    }

    @Test
    public void sameTransactionCantBeAddedTwice() {
        // given
        BankTransaction bankTransaction = getTestTransaction();

        // when
        int sid = manager.startImportSession();
        manager.addTransaction(sid, bankTransaction);
        boolean isValid = manager.isValid(sid, bankTransaction);

        // then
        assertFalse(isValid);
    }

    @Test
    public void multipleTransactionsWithAtLeastOneDifferentFieldCanBeAdded() {
        // given
        List<BankTransaction> transactions = getTestTransactions();

        // when
        int sid = manager.startImportSession();
        transactions.forEach(transaction -> manager.addTransaction(sid, transaction));
        TreeSet<BankTransaction> addedTransactions = manager.getTransactions();

        // then
        assertEquals(3, addedTransactions.size());
    }


    @Test
    public void transactionsAreNotImplicitlyAddedToView() {
        // given
        BankTransaction transaction = getTestTransaction();

        // when
        int sid = manager.startImportSession();
        manager.addTransaction(sid, transaction);

        List<BankTransaction> addedTransactions = manager.getTransactionObservableList();

        // then
        assertEquals(0, addedTransactions.size());
    }


    @Test
    public void transactionsCanBeAddedToView() {
        // given
        BankTransaction transaction = getTestTransaction();

        // when
        int sid = manager.startImportSession();
        manager.addTransaction(sid, transaction);
        manager.addToView(transaction);

        List<BankTransaction> addedTransactions = manager.getTransactionObservableList();

        // then
        assertEquals(1, addedTransactions.size());
    }

    @Test
    public void balanceIsUpdatedAfterTransactionIsAddedToView() {
        // given
        BankTransaction transaction = getTestTransaction();

        // when
        int sid = manager.startImportSession();
        manager.addTransaction(sid, transaction);
        manager.addToView(transaction);

        // then
        assertEquals(transaction.getAmount(), manager.balanceProperty().getValue());
    }


    @Test
    public void balanceIsNotUpdatedBeforeTransactionIsAddedToView() {
        // given
        BankTransaction transaction = getTestTransaction();

        // when
        int sid = manager.startImportSession();
        manager.addTransaction(sid, transaction);

        // then
        assertEquals(BigDecimal.ZERO, manager.balanceProperty().getValue());
    }


    @Test
    public void importCanBeReversedBeforeCompletion() {
        // given
        List<BankTransaction> transactions = getTestTransactions();
        int sid = manager.startImportSession();
        transactions.forEach(transaction -> manager.addTransaction(sid, transaction));
        transactions.forEach(transaction -> manager.addToView(transaction));

        // when
        manager.reverseImport(sid);

        TreeSet<BankTransaction> addedTransactions = manager.getTransactions();
        List<BankTransaction> addedTransactionsView = manager.getTransactionObservableList();
        BigDecimal balance = manager.balanceProperty().getValue();

        // then
        assertEquals(0, addedTransactions.size());
        assertEquals(0, addedTransactionsView.size());
        assertEquals(BigDecimal.ZERO, balance);
    }


    @Test
    public void transactionCanUpdated() {
        // given
        BankTransaction transaction = getTestTransaction();
        int sid = manager.startImportSession();
        manager.addTransaction(sid, transaction);
        manager.addToView(transaction);

        BankTransaction editedTransaction = copyBankTransaction(transaction);
        editedTransaction.setDescription(transaction.getDescription() + "different");

        // when
        manager.updateTransaction(transaction, editedTransaction);
        List<BankTransaction> transactionsInView = manager.getTransactionObservableList();
        TreeSet<BankTransaction> transactions = manager.getTransactions();

        // then
        assertEquals(1, transactionsInView.size());
        assertEquals(editedTransaction, transactionsInView.get(0));
        assertEquals(1, transactions.size());
        assertEquals(editedTransaction, transactions.first());
    }

    @Test
    public void sameTransactionCanBeAddedAgainIfOriginalWasUpdated() {
        // given
        BankTransaction transaction = getTestTransaction();
        int sid = manager.startImportSession();
        manager.addTransaction(sid, transaction);
        manager.addToView(transaction);

        BankTransaction copy = copyBankTransaction(transaction);

        BankTransaction editedTransaction = copyBankTransaction(transaction);
        editedTransaction.setDescription(editedTransaction.getDescription() + "different");

        // when
        manager.updateTransaction(transaction, editedTransaction);
        boolean isValid = manager.isValid(copy);

        // then
        assertTrue(isValid);
    }

    private BankTransaction getTestTransaction() {
       BankTransaction transaction = new BankTransaction("abc", BigDecimal.valueOf(123), LocalDate.now());
       BankStatement statement = mock(BankStatement.class);
       when(statement.getPeriodStartDate()).thenReturn(LocalDate.now());
       when(statement.getPeriodEndDate()).thenReturn(LocalDate.now());

       transaction.setBankStatement(statement);
       return transaction;
    }

    private BankTransaction copyBankTransaction(BankTransaction transaction) {
        BankTransaction copy = new BankTransaction(transaction.getDescription(),
                transaction.getAmount(), transaction.getDate());
        copy.setBankStatement(transaction.getBankStatement());

        return copy;
    }

    private List<BankTransaction> getTestTransactions() {
        BankTransaction bankTransaction1 = getTestTransaction();
        BankTransaction bankTransaction2 = new BankTransaction("bcd", BigDecimal.valueOf(123), LocalDate.now());
        BankTransaction bankTransaction3 = new BankTransaction("bcd", BigDecimal.valueOf(777), LocalDate.now());
        return List.of(bankTransaction1, bankTransaction2, bankTransaction3);
    }
}
