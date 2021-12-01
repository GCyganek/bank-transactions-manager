package repository.dao;

import model.BankStatement;
import model.BankTransaction;

import java.util.Optional;

public interface BankStatementDao {
    BankStatement create(BankStatement bankStatement);

    Optional<BankStatement> findById(int id);

    void addTransaction(BankStatement bankStatement, BankTransaction bankTransaction);

    void remove(BankStatement bankStatement);
}
