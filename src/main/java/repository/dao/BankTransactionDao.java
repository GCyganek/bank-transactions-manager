package repository.dao;

import model.BankTransaction;
import session.HibernateSessionService;

import javax.persistence.PersistenceException;
import java.util.Optional;

public class BankTransactionDao extends AbstractDao<BankTransaction> {

    public BankTransaction create(BankTransaction bankTransaction) {
        try {
            save(bankTransaction);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return bankTransaction;
    }

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
