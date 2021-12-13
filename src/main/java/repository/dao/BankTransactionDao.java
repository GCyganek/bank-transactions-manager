package repository.dao;

import model.BankTransaction;

import java.util.List;
import java.util.Optional;

public interface BankTransactionDao {

    BankTransaction create(BankTransaction bankTransaction);

    Optional<BankTransaction> findById(int id);

    List<BankTransaction> getAll();

    void remove(BankTransaction bankTransaction);

    void updateTransaction(BankTransaction bankTransaction);
}
