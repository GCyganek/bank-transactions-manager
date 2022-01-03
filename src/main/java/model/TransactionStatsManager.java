package model;

import io.reactivex.rxjava3.core.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import model.util.ModelUtil;
import model.util.TransactionCategory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeSet;

public class TransactionStatsManager {
    private final TreeSet<BankTransaction> transactions;
    private BigDecimal totalIncome, totalOutcome;

    @Inject
    public TransactionStatsManager(TransactionsManager transactionsManager){
        transactions = new TreeSet<>(ModelUtil.getDateThenAmountThenDescriptionComparator());
        totalIncome = BigDecimal.ZERO;
        totalOutcome = BigDecimal.ZERO;

        ObservableList<BankTransaction> transactionObservableList = transactionsManager.getTransactionObservableList();
        Observable<Pair<BankTransaction, BankTransaction>> transactionUpdatedObservable
                = transactionsManager.getTransactionUpdatedObservable();

        setupTransactionListeners(transactionObservableList, transactionUpdatedObservable);
    }


    private void setupTransactionListeners(ObservableList<BankTransaction> transactionObservableList,
                                           Observable<Pair<BankTransaction, BankTransaction>> transactionUpdatedObservable)
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

        transactionUpdatedObservable.subscribe(update -> {
            BankTransaction old = update.getKey();
            BankTransaction edited = update.getValue();
            removeTransaction(old);
            addTransaction(edited);
        });
    }


    private void addTransaction(BankTransaction transaction) {
        transactions.add(transaction);

        // if needed update aggregated params here
        updateTotalIncomeOutcomeOnAdd(transaction.getAmount());
    }

    private void removeTransaction(BankTransaction transaction) {
        transactions.remove(transaction);

        // if needed update aggregated params here
        updateTotalIncomeOutcomeOnRemove(transaction.getAmount());
    }

    private void updateTotalIncomeOutcomeOnRemove(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            totalOutcome = totalOutcome.add(value);
        }
        else {
            totalIncome = totalIncome.subtract(value);
        }

    }

    private void updateTotalIncomeOutcomeOnAdd(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            totalOutcome = totalOutcome.subtract(value);
        }
        else {
            totalIncome = totalIncome.add(value);
        }
    }

    public BigDecimal getIncome(LocalDate fromDate, LocalDate toDate) {
        return transactions.stream()
                .filter(x -> x.isBetweenDates(fromDate, toDate))
                .map(BankTransaction::getAmount)
                .filter(x -> x.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getOutcome(LocalDate fromDate, LocalDate toDate) {
        return BigDecimal.ZERO.subtract(transactions.stream()
                .filter(x -> x.isBetweenDates(fromDate, toDate))
                .map(BankTransaction::getAmount)
                .filter(x -> x.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal getTotalOutcome() {
        return totalOutcome;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public Optional<LocalDate> getCurrentStartDate() {
        return transactions.isEmpty() ? Optional.empty() : Optional.of(transactions.first().getDate());
    }

    public Optional<LocalDate> getCurrentEndDate() {
        return transactions.isEmpty() ? Optional.empty() : Optional.of(transactions.last().getDate());
    }

    public HashMap<TransactionCategory, BigDecimal> getOutcomesInCategories(LocalDate fromDate, LocalDate toDate) {
        HashMap<TransactionCategory, BigDecimal> outcomesInCategories = new HashMap<>();

        for(TransactionCategory category: TransactionCategory.values()) {
            outcomesInCategories.put(category, BigDecimal.ZERO);
        }

        for(BankTransaction transaction: transactions) {
            if(transaction.isBetweenDates(fromDate, toDate)
                    && transaction.getAmount().compareTo(BigDecimal.ZERO) < 0)
            {
                outcomesInCategories.put(transaction.getCategory(),
                        outcomesInCategories.get(transaction.getCategory()).subtract(transaction.getAmount()));
            }
        }

        return outcomesInCategories;
    }

    public TreeSet<BankTransaction> getTransactions() {
        return transactions;
    }
}
