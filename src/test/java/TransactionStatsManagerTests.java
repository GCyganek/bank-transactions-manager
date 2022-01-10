import model.*;
import model.util.ImportSession;
import model.util.TransactionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.BankStatementsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionStatsManagerTests {

    private TransactionStatsManager transactionStatsManager;
    private TransactionsSupervisor transactionsSupervisor;
    private Account account;

    @BeforeEach
    public void initManagers() {
        BankStatementsRepository repository = mock(BankStatementsRepository.class);
        account = new Account(repository);
        transactionsSupervisor = new TransactionsSupervisor(repository, account);
        transactionStatsManager = new TransactionStatsManager(account);
    }

    @Test
    public void transactionStatsManagerUpdatesOnAddToViewInTransactionManager() {
        // given
        BankTransaction testTransaction = getTestTransaction();

        // when
        account.addTransaction(testTransaction);
        TreeSet<BankTransaction> transactions = transactionStatsManager.getTransactions();

        // then
        assertEquals(1, transactions.size());
        assertEquals(BigDecimal.valueOf(123), transactionStatsManager.getTotalIncome());
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getTotalOutcome());
    }

    @Test
    public void incomeOutcomeUpdatesOnTransactionEdit() {
        // given
        BankTransaction transaction = getTestTransaction();
        ImportSession importSession = transactionsSupervisor.startImportSession();
        transactionsSupervisor.tryToAddTransaction(importSession, transaction);
        account.addTransaction(transaction);

        BankTransaction editedTransaction = transaction.shallowCopy();
        editedTransaction.setAmount(BigDecimal.valueOf(-123));

        // when
        transactionsSupervisor.updateTransaction(transaction, editedTransaction);
        TreeSet<BankTransaction> transactions = transactionStatsManager.getTransactions();

        // then
        assertEquals(1, transactions.size());
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getTotalIncome());
        assertEquals(BigDecimal.valueOf(123), transactionStatsManager.getTotalOutcome());
    }

    @Test
    public void transactionStatsManagerUpdatesOnReverseImportInTransactionManager() {
        // given
        List<BankTransaction> transactions = getTestTransactions();
        ImportSession importSession = transactionsSupervisor.startImportSession();
        transactions.forEach(transaction -> transactionsSupervisor.tryToAddTransaction(importSession, transaction));
        transactions.forEach(transaction -> account.addTransaction(transaction));

        TreeSet<BankTransaction> transactionsFromStatsManager = transactionStatsManager.getTransactions();

        assertEquals(4, transactionsFromStatsManager.size());
        assertEquals(BigDecimal.valueOf(178), transactionStatsManager.getTotalIncome());
        assertEquals(BigDecimal.valueOf(810), transactionStatsManager.getTotalOutcome());

        // when
        transactionsSupervisor.reverseImport(importSession);
        transactionsFromStatsManager = transactionStatsManager.getTransactions();

        // then
        assertEquals(0, transactionsFromStatsManager.size());
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getTotalIncome());
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getTotalOutcome());
    }

    @Test
    public void startAndEndDateTest() {
        // given
        List<BankTransaction> transactions = getTestTransactions();

        // when
        transactions.forEach(transaction -> account.addTransaction(transaction));

        // then
        assertEquals(LocalDate.of(2021, 12, 18), transactionStatsManager.getCurrentStartDate().get());
        assertEquals(LocalDate.of(2021, 12, 23), transactionStatsManager.getCurrentEndDate().get());
    }

    @Test
    public void startAndEndDateTestAfterUpdate() {
        // given
        List<BankTransaction> transactions = getTestTransactions();
        ImportSession importSession = transactionsSupervisor.startImportSession();
        transactions.forEach(transaction -> transactionsSupervisor.tryToAddTransaction(importSession, transaction));
        transactions.forEach(transaction -> account.addTransaction(transaction));

        BankTransaction transactionToEdit = transactions.get(1);
        BankTransaction editedTransaction = transactionToEdit.shallowCopy();
        editedTransaction.setDate(LocalDate.of(2021, 12, 25));

        // when
        transactionsSupervisor.updateTransaction(transactionToEdit, editedTransaction);

        // then
        assertEquals(LocalDate.of(2021, 12, 20), transactionStatsManager.getCurrentStartDate().get());
        assertEquals(LocalDate.of(2021, 12, 25), transactionStatsManager.getCurrentEndDate().get());
    }

    @Test
    public void getOutcomesInCategoriesTest() {
        // given
        List<BankTransaction> transactions = getTestTransactions();

        // when
        transactions.forEach(transaction -> account.addTransaction(transaction));
        HashMap<TransactionCategory, BigDecimal> outcomesInCategories = transactionStatsManager
                .getOutcomesInCategories(LocalDate.of(2021, 12, 1),
                                         LocalDate.of(2021, 12, 31));

        // then
        assertEquals(BigDecimal.ZERO, outcomesInCategories.get(TransactionCategory.UNCATEGORIZED));
        assertEquals(BigDecimal.valueOf(33), outcomesInCategories.get(TransactionCategory.HEALTH_AND_BEAUTY));
        assertEquals(BigDecimal.ZERO, outcomesInCategories.get(TransactionCategory.CLOTHES_AND_SHOES));
        assertEquals(BigDecimal.valueOf(777), outcomesInCategories.get(TransactionCategory.FOOD));
    }

    @Test
    public void getOutcomeWithDifferentDateRangesTest() {
        // given
        List<BankTransaction> transactions = getTestTransactions();

        // when
        transactions.forEach(transaction -> account.addTransaction(transaction));

        // then
        assertEquals(BigDecimal.valueOf(810), transactionStatsManager.getOutcome(LocalDate.of(2021, 12, 1),
                LocalDate.of(2021, 12, 31)));
        assertEquals(BigDecimal.valueOf(33), transactionStatsManager.getOutcome(LocalDate.of(2021, 12, 22),
                LocalDate.of(2021, 12, 22)));
        assertEquals(BigDecimal.valueOf(777), transactionStatsManager.getOutcome(LocalDate.of(2021, 12, 23),
                LocalDate.of(2021, 12, 23)));
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getOutcome(LocalDate.of(2021, 12, 1),
                LocalDate.of(2021, 12, 21)));
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getOutcome(LocalDate.of(2021, 12, 24),
                LocalDate.of(2021, 12, 31)));
    }

    @Test
    public void getIncomeWithDifferentDateRangesTest() {
        // given
        List<BankTransaction> transactions = getTestTransactions();

        // when
        transactions.forEach(transaction -> account.addTransaction(transaction));

        // then
        assertEquals(BigDecimal.valueOf(178), transactionStatsManager.getIncome(LocalDate.of(2021, 12, 1),
                LocalDate.of(2021, 12, 31)));
        assertEquals(BigDecimal.valueOf(55), transactionStatsManager.getIncome(LocalDate.of(2021, 12, 18),
                LocalDate.of(2021, 12, 18)));
        assertEquals(BigDecimal.valueOf(123), transactionStatsManager.getIncome(LocalDate.of(2021, 12, 20),
                LocalDate.of(2021, 12, 20)));
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getIncome(LocalDate.of(2021, 12, 1),
                LocalDate.of(2021, 12, 17)));
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getIncome(LocalDate.of(2021, 12, 19),
                LocalDate.of(2021, 12, 19)));
        assertEquals(BigDecimal.ZERO, transactionStatsManager.getIncome(LocalDate.of(2021, 12, 21),
                LocalDate.of(2021, 12, 31)));
    }

    private BankTransaction getTestTransaction() {
        BankTransaction transaction =
                new BankTransaction("abc", BigDecimal.valueOf(123), LocalDate.of(2021, 12, 20));
        transaction.setCategory(TransactionCategory.FOOD);
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
        BankTransaction bankTransaction2 =
                new BankTransaction("bcd", BigDecimal.valueOf(55), LocalDate.of(2021, 12, 18));
        bankTransaction2.setCategory(TransactionCategory.CLOTHES_AND_SHOES);
        BankTransaction bankTransaction3 =
                new BankTransaction("bcd", BigDecimal.valueOf(-777), LocalDate.of(2021, 12, 23));
        bankTransaction3.setCategory(TransactionCategory.FOOD);
        BankTransaction bankTransaction4 =
                new BankTransaction("bcd", BigDecimal.valueOf(-33), LocalDate.of(2021, 12, 22));
        bankTransaction4.setCategory(TransactionCategory.HEALTH_AND_BEAUTY);
        BankStatement statement = getMockStatement();

        List<BankTransaction> result = List.of(bankTransaction1, bankTransaction2, bankTransaction3, bankTransaction4);
        result.forEach(transaction -> transaction.setBankStatement(statement));

        return result;
    }


}
