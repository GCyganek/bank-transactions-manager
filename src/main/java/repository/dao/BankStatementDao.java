package repository.dao;

import model.BankStatement;
import model.BankTransaction;

import java.util.List;
import java.util.Optional;

public interface BankStatementDao {
    BankStatement create(BankStatement bankStatement);

    Optional<BankStatement> findById(int id);

    void addTransaction(BankStatement bankStatement, BankTransaction bankTransaction);

    void remove(BankStatement bankStatement);

    void updateStatement(BankStatement bankStatement);

    List<BankStatement> getAllStatements();

    List<BankTransaction> getAllTransactionsFromStatement(int id);
}
