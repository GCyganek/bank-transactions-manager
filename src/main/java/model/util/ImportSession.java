package model.util;

import model.BankTransaction;

import java.util.LinkedList;
import java.util.List;

public class ImportSession {
    private final List<BankTransaction> importedTransactions;
    private int filteredTransactionsCount;

    public ImportSession() {
        importedTransactions = new LinkedList<>();
        filteredTransactionsCount = 0;
    }

    public void addTransaction(BankTransaction transaction) {
        this.importedTransactions.add(transaction);
    }

    public List<BankTransaction> getImportedTransactions() {
        return importedTransactions;
    }

    public void addFilteredTransaction(BankTransaction transaction) {
        // for now, we need only count of those transactions
        filteredTransactionsCount++;
    }

    public int getFilteredTransactionsCount() {
        return filteredTransactionsCount;
    }
}
