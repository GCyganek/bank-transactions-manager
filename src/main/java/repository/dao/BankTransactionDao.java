package repository.dao;

import model.BankTransaction;

import java.util.Optional;

public interface BankTransactionDao {
    BankTransaction create(BankTransaction bankTransaction);

    Optional<BankTransaction> findById(int id);
}
