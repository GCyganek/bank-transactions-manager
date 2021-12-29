package model;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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

        setupTransactionListeners(transactionObservableList);
    }


    private void setupTransactionListeners(ObservableList<BankTransaction> transactionObservableList)
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
    }


    private void addTransaction(BankTransaction transaction) {
        transactions.add(transaction);
        setupTransactionPropertyBindings(transaction);

        // if needed update aggregated params here
        updateTotalIncomeOutcome(transaction.getAmount());
    }

    private void removeTransaction(BankTransaction transaction) {
        transactions.remove(transaction);

        // if needed update aggregated params here
        updateTotalIncomeOutcome(transaction.getAmount(), BigDecimal.ZERO);
    }

    private void setupTransactionPropertyBindings(BankTransaction transaction) {
        transaction.amountProperty().addListener((observable, oldValue, newValue) -> {
            updateTotalIncomeOutcome(oldValue, newValue);
        });
    }


    private void updateTotalIncomeOutcome(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) < 0) {
            totalOutcome = totalOutcome.add(oldValue);
        }
        else {
            totalIncome = totalIncome.subtract(oldValue);
        }

        updateTotalIncomeOutcome(newValue);
    }

    private void updateTotalIncomeOutcome(BigDecimal newValue) {
        if (newValue.compareTo(BigDecimal.ZERO) < 0) {
            totalOutcome = totalOutcome.subtract(newValue);
        }
        else {
            totalIncome = totalIncome.add(newValue);
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
        HashMap<TransactionCategory, BigDecimal> map = new HashMap<>();

        for(TransactionCategory category: TransactionCategory.values()) {
            map.put(category, BigDecimal.ZERO);
        }

        for(BankTransaction transaction: transactions) {
            if(transaction.isBetweenDates(fromDate, toDate)
                    && transaction.getAmount().compareTo(BigDecimal.ZERO) < 0)
            {
                map.put(transaction.getCategory(), map.get(transaction.getCategory()).subtract(transaction.getAmount()));
            }
        }

        return map;
    }

}
