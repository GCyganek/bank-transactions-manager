package repository;

import model.BankStatement;
import model.BankTransaction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BankStatementsRepository {
    BankStatement addStatementWithTransactions(BankStatement bankStatement, Collection<BankTransaction> bankTransactions);

    BankStatement addBankStatement(BankStatement bankStatement);

    Optional<BankStatement> getStatementById(int id);

    List<BankStatement> getAllStatements();

    List<BankTransaction> getAllTransactions();

    void removeStatement(BankStatement bankStatement);

    void removeAllStatements();
}
