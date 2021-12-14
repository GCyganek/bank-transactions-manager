package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class TransactionsManager {
    private final ObservableList<BankTransaction> transactionsObs;
    private final ObjectProperty<BigDecimal> balance;

    private final BankStatementsRepository bankStatementsRepository;
    private final TreeSet<BankTransaction> transactions;


    @Inject
    public TransactionsManager(BankStatementsRepository repository,
                               Comparator<BankTransaction> transactionComparator) {
        bankStatementsRepository = repository;
        transactionsObs = FXCollections.observableArrayList();
        transactions = new TreeSet<>(transactionComparator);
        balance = new SimpleObjectProperty<>();
    }

    public void fetchDataFromDatabase() {
        List<BankTransaction> storedTransactions = bankStatementsRepository.getAllTransactions();
        transactionsObs.addAll(storedTransactions);

        BigDecimal totalBalance = storedTransactions.stream()
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // no need to update view after each amount is added
        addToBalance(totalBalance);
    }

    public boolean isValid(BankTransaction transaction) {
        return true;
    }

    public void addTransaction(BankTransaction transaction) {
        transactions.add(transaction);
        transactionsObs.add(transaction);
    }


    private void addToBalance(BigDecimal amount) {
        balance.set(balance.getValue().add(amount));
    }
}
