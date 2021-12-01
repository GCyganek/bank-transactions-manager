package repository.dao;

import model.BankTransaction;
import session.HibernateSessionService;

import javax.persistence.PersistenceException;
import java.util.Optional;

/**
 * Postgres DAO
 */
public class PgBankTransactionDao extends AbstractDao<BankTransaction> implements BankTransactionDao {

    @Override
    public BankTransaction create(BankTransaction bankTransaction) {
        try {
            save(bankTransaction);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return bankTransaction;
    }

    @Override
    public Optional<BankTransaction> findById(int id) {
        try {
            BankTransaction bankTransaction = HibernateSessionService.getSession()
                    .createQuery("SELECT bt FROM BankTransaction bt WHERE bt.id = :id", BankTransaction.class)
                    .setParameter("id", id).getSingleResult();
            return Optional.of(bankTransaction);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
