package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.util.ImportSession;
import model.util.ModelUtil;
import model.util.TransactionCategory;
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

    // contains every transaction sorted by date, used to ensure uniqueness
    private final TreeSet<BankTransaction> transactions;

    // used to prevent updating transaction in repository
    // before statement had been fully imported (statement is persisted after import completes)
    private final HashSet<BankStatement> importInProgressStatements;

    @Inject
    public TransactionsManager(BankStatementsRepository repository) {
        bankStatementsRepository = repository;
        transactionObservableList = FXCollections.observableArrayList();
        transactions = new TreeSet<>(ModelUtil.getDateComparator());
        balance = new SimpleObjectProperty<>();
        balance.set(BigDecimal.ZERO);

        importInProgressStatements = new HashSet<>();
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

    /**
     * Each import has its unique session, every succeeding call to manager from scope
     * of that import has to use ImportSession returned from this function.
     */
    public ImportSession startImportSession() {
        return new ImportSession();
    }


    private boolean isValid(BankTransaction bankTransaction) {
       return isUnique(bankTransaction);
    }

    /**
     * Adds transaction to model only if it is valid.
     * @return true if transaction was added to model and thus can be added to view
     */
    public synchronized boolean tryToAddTransaction(ImportSession importSession, BankTransaction transaction) {
        // adding and checking have to be done in one call to prevent race condition
        if (!isValid(transaction)) {
            importSession.addFilteredTransaction(transaction);
            return false;
        }

        importSession.addTransaction(transaction);
        transactions.add(transaction);

        BankStatement statement = transaction.getBankStatement();
        statement.addBankTransaction(transaction);
        importInProgressStatements.add(statement);

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
    public synchronized int completeImport(ImportSession importSession) {
        List<BankTransaction> importedTransactions = importSession.getImportedTransactions();

        // persist statement iff it contains >= 1 transactions
        if (!importedTransactions.isEmpty()) {
            BankStatement bankStatement = importedTransactions.get(0).getBankStatement();
            bankStatementsRepository.addBankStatement(bankStatement);
            importInProgressStatements.remove(bankStatement);
        }

        return importSession.getFilteredTransactionsCount();
    }

    /**
     * Removes every transaction already added in this import session from both view and model, has to be used at most once.
     */
    public synchronized void reverseImport(ImportSession importSession) {
        List<BankTransaction> addedTransactions = importSession.getImportedTransactions();

        if (!addedTransactions.isEmpty()) {
            importInProgressStatements.remove(addedTransactions.get(0).getBankStatement());

            // removing from this list shouldn't be that common, so performance shouldn't be degraded
            addedTransactions.forEach(transactions::remove);
            addedTransactions.forEach(transactionObservableList::remove);

            balance.set(balance.getValue().subtract(getTotalBalance(addedTransactions)));
        }
    }

    /**
     * @param old    - transaction before any params have been edited
     * @param edited - same (i.e. copied) transaction with edited params
     * @return true iff edited transaction was updated
     */
    public synchronized boolean updateTransaction(BankTransaction old, BankTransaction edited) {
        if (!isValid(edited))
           return false;

        // has to be removed to keep TreeSet structure valid
        transactions.remove(old);

        // edit params of old transaction to keep references in other objects valid
        old.copyEditableFieldsFrom(edited);
        transactions.add(old);

        boolean statementUpdateNeeded = fixStatementPaidInOut(edited.getAmount(), old) ||
                                        fixStatementDate(old);

        // can't update transaction in repo if statement is still being imported
        if (!importInProgressStatements.contains(old.getBankStatement())) {
            bankStatementsRepository.updateTransaction(old);

            if (statementUpdateNeeded) {
                bankStatementsRepository.updateStatement(old.getBankStatement());
            }
        }

        return true;
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

    public BigDecimal getTotalIncome() {
        return transactions.stream()
                .map(BankTransaction::getAmount)
                .filter(x -> x.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalOutcome() {
        return BigDecimal.ZERO.subtract(transactions.stream()
                                        .map(BankTransaction::getAmount)
                                        .filter(x -> x.compareTo(BigDecimal.ZERO) < 0)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public LocalDate getCurrentStartDate() {
        return transactions.stream()
                .map(BankTransaction::getDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.of(2000,1,1));
    }

    public LocalDate getCurrentEndDate() {
        return transactions.stream()
                .map(BankTransaction::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.of(2000,1,1));
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
