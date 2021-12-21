package model;

import io.reactivex.rxjava3.core.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.util.ModelUtil;
import model.util.TransactionCategory;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.TreeSet;

public class TransactionStatsManager {
    private final TreeSet<BankTransaction> transactions;

    @Inject
    public TransactionStatsManager(TransactionsManager transactionsManager){
        transactions = new TreeSet<>(ModelUtil.getDateComparator());
        ObservableList<BankTransaction> transactionObservableList = transactionsManager.getTransactionObservableList();

        setupTransactionListeners(transactionObservableList, transactionsManager.getTransactionUpdatedObservable());
    }


    private void setupTransactionListeners(ObservableList<BankTransaction> transactionObservableList,
                                           Observable<Pair<BankTransaction, BankTransaction>> transactionUpdatedObs)
    {
        transactionObservableList.addListener((ListChangeListener<BankTransaction>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(this::addTransaction);
                }
                else if (c.wasRemoved()) {
                    c.getRemoved().forEach(this::removeTransaction);
                }
            }
        });

        transactionUpdatedObs
                .subscribe(update -> {
                    BankTransaction old = update.getLeft();
                    BankTransaction updated = update.getRight();

                    removeTransaction(old);
                    addTransaction(updated);
                });
    }


    private void addTransaction(BankTransaction transaction) {
        transactions.add(transaction);
        // todo if needed update aggregated params here
    }

    private void removeTransaction(BankTransaction transaction) {
        transactions.remove(transaction);
        // todo if needed update aggregated params here
    }

    public BigDecimal getIncome(LocalDate fromDate, LocalDate toDate) {
        return transactions.stream()
                .filter(x -> x.getDate().isAfter(fromDate) && x.getDate().isBefore(toDate))
                .map(BankTransaction::getAmount)
                .filter(x -> x.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getOutcome(LocalDate fromDate, LocalDate toDate) {
        return BigDecimal.ZERO.subtract(transactions.stream()
                .filter(x -> x.getDate().isAfter(fromDate) && x.getDate().isBefore(toDate))
                .map(BankTransaction::getAmount)
                .filter(x -> x.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal getTotalOutcome() {
        return BigDecimal.ZERO.subtract(transactions.stream()
                .map(BankTransaction::getAmount)
                .filter(x -> x.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public LocalDate getCurrentStartDate() {
        return transactions.isEmpty() ? LocalDate.of(2000,1,1) : transactions.first().getDate();
    }

    public LocalDate getCurrentEndDate() {
        return transactions.isEmpty() ? LocalDate.of(2000,1,1) : transactions.last().getDate();
    }

    public HashMap<TransactionCategory, BigDecimal> getOutcomesInCategories() {
        HashMap<TransactionCategory, BigDecimal> map = new HashMap<>();

        for(TransactionCategory category: TransactionCategory.values()) {
            map.put(category, BigDecimal.ZERO);
        }

        for(BankTransaction transaction: transactions) {
            if(transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                map.put(transaction.getCategory(), map.get(transaction.getCategory()).subtract(transaction.getAmount()));
            }
        }

        return map;
    }

}
