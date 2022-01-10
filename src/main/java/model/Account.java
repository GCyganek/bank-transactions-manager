package model;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Singleton
public class Account {
    private final BankStatementsRepository bankStatementsRepository;

    private final ObservableList<BankTransaction> transactionObservableList;
    private final ObjectProperty<BigDecimal> balance;
    private final PublishSubject<Pair<BankTransaction, BankTransaction>> transactionUpdatedSubject;

    @Inject
    public Account(BankStatementsRepository repository) {
        this.bankStatementsRepository = repository;

        transactionObservableList = FXCollections.observableArrayList();
        balance = new SimpleObjectProperty<>();
        transactionUpdatedSubject = PublishSubject.create();

        initAccountData();
    }

    private void initAccountData() {
        List<BankTransaction> storedTransactions = bankStatementsRepository.getAllTransactions();
        transactionObservableList.addAll(storedTransactions);
        BigDecimal totalBalance = getTotalBalance(storedTransactions);

        // no need to update view after each amount is added
        balance.set(totalBalance);
    }

    public void addTransaction(BankTransaction transaction) {
        transactionObservableList.add(transaction);
        addToBalance(transaction.getAmount());
    }

    /**
     * @return true iff statement was edited
     */
    public void onTransactionEdited(BankTransaction old, BankTransaction edited, boolean isStatementUpdateNeeded) {
        transactionUpdatedSubject.onNext(new Pair<>(old, edited));

        if (isStatementUpdateNeeded) {
            fixStatementDate(edited);
            fixStatementPaidInOut(old.getAmount(), edited);
        }
    }

    public void removeTransaction(BankTransaction transaction) {
        transactionObservableList.remove(transaction);
        subtractFromBalance(transaction.getAmount());
    }

    public void addToBalance(BigDecimal amount) {
        balance.set(balance.getValue().add(amount));
    }

    public void subtractFromBalance(BigDecimal amount) {
        balance.set(balance.getValue().subtract(amount));
    }


    private BigDecimal getTotalBalance(List<BankTransaction> transactions) {
        return transactions.stream()
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<BankTransaction> getTransactions() {
        return transactionObservableList.stream().toList();
    }

    public ObservableList<BankTransaction> getTransactionObservableList() {
        return transactionObservableList;
    }

    public ObjectProperty<BigDecimal> balanceProperty() {
        return balance;
    }

    public Observable<Pair<BankTransaction, BankTransaction>> getTransactionUpdatedObservable() {
        return Observable.wrap(transactionUpdatedSubject);
    }

    public boolean isBankStatementUpdateNeeded(BankTransaction old, BankTransaction edited) {
        BankStatement statement = old.getBankStatement();

        return !old.getAmount().equals(edited.getAmount()) ||
                edited.getDate().isAfter(statement.getPeriodEndDate()) ||
                edited.getDate().isBefore(statement.getPeriodStartDate());
    }

    private void fixStatementDate(BankTransaction bankTransaction) {
        BankStatement bankStatement = bankTransaction.getBankStatement();
        LocalDate statementPeriodEndDate = bankStatement.getPeriodEndDate();
        LocalDate statementPeriodStartDate = bankStatement.getPeriodStartDate();
        LocalDate transactionDate = bankTransaction.getDate();

        if (transactionDate.isAfter(statementPeriodEndDate)) {
            bankStatement.setPeriodEndDate(transactionDate);
        }

        if (transactionDate.isBefore(statementPeriodStartDate)) {
            bankStatement.setPeriodStartDate(transactionDate);
        }
    }

    private void fixStatementPaidInOut(BigDecimal amountBeforeEdit, BankTransaction bankTransaction) {
        BigDecimal amountAfterEdit = bankTransaction.getAmount();

        if (amountAfterEdit.compareTo(amountBeforeEdit) == 0) return;

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
    }
}
