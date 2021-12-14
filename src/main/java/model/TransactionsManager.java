package model;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

public class TransactionsManager {

    private final ObservableList<BankTransaction> transactionObservableList;
    private final ObjectProperty<BigDecimal> balance;

    private final BankStatementsRepository bankStatementsRepository;
    private final TreeSet<BankTransaction> transactions;

    private final HashMap<Integer, List<BankTransaction>> importSessions;
    private final HashMap<Integer, Integer> filteredTransactionsCounts;

    private int sessionCounter;

    @Inject
    public TransactionsManager(BankStatementsRepository repository,
                              @Named("transactionComparator") Comparator<BankTransaction> transactionComparator) {
        bankStatementsRepository = repository;
        transactionObservableList = FXCollections.observableArrayList();
        transactions = new TreeSet<>(transactionComparator);
        balance = new SimpleObjectProperty<>();

        importSessions = new HashMap<>();
        filteredTransactionsCounts = new HashMap<>();

        sessionCounter = 0;
    }


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

    public synchronized boolean isValid(int sessionId, BankTransaction transaction) {
        boolean valid = !isDuplicated(transaction);
        if (!valid) {
            // increment counter
            filteredTransactionsCounts.merge(sessionId, 1, Integer::sum);
        }
        return valid;
    }

    public synchronized void addTransaction(int sessionId, BankTransaction transaction) {
        List<BankTransaction> session = importSessions.get(sessionId);
        session.add(transaction);

        transactions.add(transaction);
    }

    // must be called from GUI thread
    public void addToView(BankTransaction transaction) {
        transactionObservableList.add(transaction);
        addToBalance(transaction.getAmount());
    }


    public synchronized int completeImport(int sessionId) {
        List<BankTransaction> importedTransactions = importSessions.remove(sessionId);
        int filteredCount = filteredTransactionsCounts.remove(sessionId);

        if (!importedTransactions.isEmpty()) {
            bankStatementsRepository.addBankStatement(importedTransactions.get(0).getBankStatement());
        }

        return filteredCount;
    }

    // must be called from GUI thread
    public synchronized void revertImport(int sessionId) {
        List<BankTransaction> addedTransactions = importSessions.remove(sessionId);
        filteredTransactionsCounts.remove(sessionId);

        // removing from this list shouldn't be that common, so performance shouldn't be degraded
        addedTransactions.forEach(transactions::remove);
        addedTransactions.forEach(transactionObservableList::remove);

        balance.set(balance.getValue().subtract(getTotalBalance(addedTransactions)));
    }



    public ObjectProperty<BigDecimal> balanceProperty() {
        return balance;
    }

    public void addToBalance(BigDecimal amount) {
        balance.set(balance.getValue().add(amount));
    }

    private BigDecimal getTotalBalance(List<BankTransaction> transactions) {
        return transactions.stream()
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isDuplicated(BankTransaction transaction) {
        return transactions.contains(transaction);
    }
}
