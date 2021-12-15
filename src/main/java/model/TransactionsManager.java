package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Singleton
public class TransactionsManager {

    private final ObservableList<BankTransaction> transactionObservableList;
    private final ObjectProperty<BigDecimal> balance;

    private final BankStatementsRepository bankStatementsRepository;

    // contains every transaction, used to ensure uniqueness
    private final TreeSet<BankTransaction> transactions;

    // sessionID -> list of transaction imported in that session,
    // used to remove partially imported statement if error occurs
    private final HashMap<Integer, List<BankTransaction>> importSessions;

    // sessionID -> count of filtered transactions
    private final HashMap<Integer, Integer> filteredTransactionsCounts;

    // bankStatement -> sessionID, used to prevent updating transaction in repository
    // before statement had been fully imported (statement is persisted after import completes)
    private final HashMap<BankStatement, Integer> statementToSession;

    private int sessionCounter;

    @Inject
    public TransactionsManager(BankStatementsRepository repository,
                              @Named("transactionComparator") Comparator<BankTransaction> transactionComparator) {
        bankStatementsRepository = repository;
        transactionObservableList = FXCollections.observableArrayList();
        transactions = new TreeSet<>(transactionComparator);
        balance = new SimpleObjectProperty<>();
        balance.set(BigDecimal.ZERO);

        importSessions = new HashMap<>();
        filteredTransactionsCounts = new HashMap<>();
        statementToSession = new HashMap<>();

        sessionCounter = 0;
    }


    /**
     * Each import has its unique session identifier, every succeeding call to manager from scope
     * of that import has to use sessionId returned from this function.
     */
    public int startImportSession() {
        int sid = sessionCounter++;
        importSessions.put(sid, new LinkedList<>());
        filteredTransactionsCounts.put(sid, 0);
        return sid;
    }


    public ObservableList<BankTransaction> fetchDataFromDatabase() {
        List<BankTransaction> storedTransactions = bankStatementsRepository.getAllTransactions();
        transactionObservableList.addAll(storedTransactions);
        transactions.addAll(storedTransactions);

        BigDecimal totalBalance = getTotalBalance(storedTransactions);

        // no need to update view after each amount is added
        balance.set(totalBalance);

        return transactionObservableList;
    }

    public boolean isValid(BankTransaction bankTransaction) {
       return isUnique(bankTransaction);
    }

    /**
     * Adds transaction to model only if it is valid, these 2 actions have to be done in one call to prevent race condition.
     * @return true if transaction was added to model and thus can be added to view
     */
    public synchronized boolean tryToAddTransaction(int sessionId, BankTransaction transaction) {
        if (!isValid(transaction)) {
            filteredTransactionsCounts.merge(sessionId, 1, Integer::sum);
            return false;
        }

        List<BankTransaction> session = importSessions.get(sessionId);
        session.add(transaction);

        BankStatement statement = transaction.getBankStatement();
        statement.addBankTransaction(transaction);

        statementToSession.put(statement, sessionId);
        transactions.add(transaction);

        return true;
    }

    // must be called from GUI thread
    public void addToView(BankTransaction transaction) {
        transactionObservableList.add(transaction);
        addToBalance(transaction.getAmount());
    }


    /**
     * Persists all transactions and statement imported in that session, has to be used at most once.
     * @return number of filtered transactions
     */
    public synchronized int completeImport(int sessionId) {
        List<BankTransaction> importedTransactions = importSessions.remove(sessionId);
        int filteredCount = filteredTransactionsCounts.remove(sessionId);

        // persist statement iff it contains >= 1 transactions
        if (!importedTransactions.isEmpty()) {
            BankStatement bankStatement = importedTransactions.get(0).getBankStatement();
            bankStatementsRepository.addBankStatement(bankStatement);
            statementToSession.remove(bankStatement);
        }

        return filteredCount;
    }

    /**
     * Removes every transaction from both view and model already added in this import session, has to be used at most once.
     */
    public synchronized void reverseImport(int sessionId) {
        List<BankTransaction> addedTransactions = importSessions.remove(sessionId);
        filteredTransactionsCounts.remove(sessionId);

        if (!addedTransactions.isEmpty()) {
            statementToSession.remove(addedTransactions.get(0).getBankStatement());

            // removing from this list shouldn't be that common, so performance shouldn't be degraded
            addedTransactions.forEach(transactions::remove);
            addedTransactions.forEach(transactionObservableList::remove);

            balance.set(balance.getValue().subtract(getTotalBalance(addedTransactions)));
        }
    }

    /**
     * @param old    - transaction before any params have been edited
     * @param edited - same transaction with edited params
     * @return true iff edited transaction was updated
     */
    public synchronized boolean updateTransaction(BankTransaction old, BankTransaction edited) {
        if (!isValid(edited))
           return false;

        // has to be removed to keep TreeSet structure valid
        transactions.remove(old);

        // edit params of old transaction to keep references in other objects valid
        old.setDate(edited.getDate());
        old.setDescription(edited.getDescription());
        old.setAmount(edited.getAmount());
        old.setCategory(edited.getCategory());

        transactions.add(old);

        boolean statementUpdateNeeded = fixStatementPaidInOut(edited.getAmount(), old) ||
                                        fixStatementDate(old);

        // can't update transaction in repo if statement is still being imported
        if (!statementToSession.containsKey(old.getBankStatement())) {
            bankStatementsRepository.updateTransaction(old);

            if (statementUpdateNeeded) {
                bankStatementsRepository.updateStatement(old.getBankStatement());
            }
        }

        return true;
    }

    /**
     * Should be used if session was requested but import didn't begin.
     */
    public void clearSession(int sessionId) {
        importSessions.remove(sessionId);
        filteredTransactionsCounts.remove(sessionId);
    }

    public ObjectProperty<BigDecimal> balanceProperty() {
        return balance;
    }

    // for tests only
    public TreeSet<BankTransaction> getTransactions() {
        return transactions;
    }

    public ObservableList<BankTransaction> getTransactionObservableList() {
        return transactionObservableList;
    }

    public void addToBalance(BigDecimal amount) {
        balance.set(balance.getValue().add(amount));
    }


    private BigDecimal getTotalBalance(List<BankTransaction> transactions) {
        return transactions.stream()
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isUnique(BankTransaction transaction) {
        return !transactions.contains(transaction);
    }

    private boolean fixStatementDate(BankTransaction bankTransaction) {
        BankStatement bankStatement = bankTransaction.getBankStatement();
        LocalDate statementPeriodEndDate = bankStatement.getPeriodEndDate();
        LocalDate statementPeriodStartDate = bankStatement.getPeriodStartDate();
        LocalDate transactionDate = bankTransaction.getDate();

        if (transactionDate.isAfter(statementPeriodEndDate)) {
            bankStatement.setPeriodEndDate(transactionDate);
            return true;
        }

        if (transactionDate.isBefore(statementPeriodStartDate)) {
            bankStatement.setPeriodStartDate(transactionDate);
            return true;
        }

        return false;
    }

    private boolean fixStatementPaidInOut(BigDecimal amountBeforeEdit, BankTransaction bankTransaction) {
        BigDecimal amountAfterEdit = bankTransaction.getAmount();

        if (amountAfterEdit.compareTo(amountBeforeEdit) == 0) return false;

        BankStatement bankStatement = bankTransaction.getBankStatement();

        if (amountAfterEdit.compareTo(BigDecimal.ZERO) > 0) {
            if (amountBeforeEdit.compareTo(BigDecimal.ZERO) > 0) {
                bankStatement.addToPaidIn(amountAfterEdit.subtract(amountBeforeEdit));
            } else {
                bankStatement.addToPaidOut(amountBeforeEdit.negate());
                bankStatement.addToPaidIn(amountAfterEdit);
            }
        }

        else  {
            if (amountBeforeEdit.compareTo(BigDecimal.ZERO) >= 0) {
                bankStatement.addToPaidIn(amountBeforeEdit.negate());
                bankStatement.addToPaidOut(amountAfterEdit);
            } else {
                bankStatement.addToPaidOut(amountAfterEdit.subtract(amountBeforeEdit));
            }
        }

        return true;
    }
}
