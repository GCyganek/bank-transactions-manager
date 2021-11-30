package repository.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import session.HibernateSessionService;

import javax.persistence.PersistenceException;

public abstract class AbstractDao<T> {

    void save(final T object) throws PersistenceException {
        Session session = HibernateSessionService.getSession();
        Transaction transaction = session.beginTransaction();
        session.save(object);
        transaction.commit();
    }

     void update(final T object) throws PersistenceException {
        Session session = HibernateSessionService.getSession();
        Transaction transaction = session.beginTransaction();
        session.update(object);
        transaction.commit();
    }

    void delete(final T object) throws PersistenceException {
        Session session = HibernateSessionService.getSession();
        Transaction transaction = session.beginTransaction();
        session.delete(object);
        transaction.commit();
    }

}
