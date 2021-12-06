package repository.dao;

import model.BankStatement;
import model.BankTransaction;
import session.HibernateSessionService;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;

/**
  * Postgres DAO
  */
public class PgBankStatementDao extends AbstractDao<BankStatement> implements BankStatementDao {

    @Override
    public BankStatement create(BankStatement bankStatement) {
        try {
            save(bankStatement);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return bankStatement;
    }

    @Override
    public Optional<BankStatement> findById(int id) {
        try {
            BankStatement bankStatement = HibernateSessionService.getSession()
                    .createQuery("SELECT bs FROM BankStatement bs WHERE bs.id = :id", BankStatement.class)
                    .setParameter("id", id).getSingleResult();
            return Optional.of(bankStatement);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void addTransaction(BankStatement bankStatement, BankTransaction bankTransaction) {
        try {
            bankStatement.getBankTransactionSet().add(bankTransaction);
            bankTransaction.setBankStatement(bankStatement);
            update(bankStatement);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(BankStatement bankStatement) {
        try {
            delete(bankStatement);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<BankStatement> getAllStatements() {
        return HibernateSessionService.getSession()
                .createQuery("SELECT bs FROM BankStatement bs", BankStatement.class).getResultList();
    }

    @Override
    public List<BankTransaction> getAllTransactionsFromStatement(int id) {
        return HibernateSessionService.getSession()
                .createQuery("SELECT bt FROM BankTransaction bt WHERE bt.bankStatement.id = :id", BankTransaction.class)
                .setParameter("id", id)
                .getResultList();
    }
}
