package model;

import model.util.ImportSession;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;

@Singleton
public class TransactionsSupervisor {
    private final BankStatementsRepository bankStatementsRepository;
    private final Account account;

    // contains every transaction, used to ensure uniqueness
    private final HashSet<BankTransaction> transactions;

    // used to prevent updating transaction in repository
    // before statement had been fully imported (statement is persisted after import completes)
    private final HashSet<BankStatement> importInProgressStatements;


    @Inject
    public TransactionsSupervisor(BankStatementsRepository repository, Account account) {
        bankStatementsRepository = repository;
        this.account = account;
        transactions = new HashSet<>(account.getTransactions());
        importInProgressStatements = new HashSet<>();
    }

    private boolean isValid(BankTransaction bankTransaction) {
        return isUnique(bankTransaction);
    }

    private boolean isUnique(BankTransaction transaction) {
        return !transactions.contains(transaction);
    }

    /**
     * Each import has its unique session, every succeeding call to manager from scope
     * of that import has to use ImportSession returned from this function.
     */
    public ImportSession startImportSession() {
        return new ImportSession();
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
            addedTransactions.forEach(account::removeTransaction);
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

        BankStatement statement = old.getBankStatement();
        boolean isImportInProgress = importInProgressStatements.contains(statement);
        boolean isStatementUpdateNeeded = account.isBankStatementUpdateNeeded(old, edited);

        removeUneditedReferences(old, statement, isStatementUpdateNeeded, isImportInProgress);

        // we need access to old fields so this should be called before actual edit
        account.onTransactionEdited(old, edited, isStatementUpdateNeeded);

        // edit params of old transaction to keep references in other objects valid
        old.copyEditableFieldsFrom(edited);

        addEditedReferences(old, statement, isStatementUpdateNeeded, isImportInProgress);

        updateRepositoryAfterEdit(old, statement, isStatementUpdateNeeded, isImportInProgress);

        return true;
    }

    private void removeUneditedReferences(BankTransaction old, BankStatement statement,
                                          boolean isStatementUpdateNeeded, boolean isImportInProgress)
    {
        // has to be removed to keep HashSet structure valid
        transactions.remove(old);

        if (isImportInProgress && isStatementUpdateNeeded) {
            importInProgressStatements.remove(statement);
        }
    }

    private void addEditedReferences(BankTransaction edited, BankStatement statement,
                              boolean isStatementUpdateNeeded, boolean isImportInProgress)
    {
        transactions.add(edited);

        if (isImportInProgress && isStatementUpdateNeeded) {
            // it was edited by Account class, and removed from this structure
            importInProgressStatements.add(statement);
        }
    }

    private void updateRepositoryAfterEdit(BankTransaction edited, BankStatement statement,
                                           boolean isStatementUpdateNeeded, boolean isImportInProgress)
    {
        // can't update transaction in repo if statement is still being imported
        if (!isImportInProgress) {
            bankStatementsRepository.updateTransaction(edited);

            if (isStatementUpdateNeeded) {
                bankStatementsRepository.updateStatement(statement);
            }
        }
    }

    // for tests only
    public HashSet<BankTransaction> getTransactions() {
        return transactions;
    }
}
