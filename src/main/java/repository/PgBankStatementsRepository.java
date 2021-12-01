package repository;

import model.BankStatement;
import model.BankTransaction;
import repository.dao.BankStatementDao;
import repository.dao.PgBankStatementDao;
import session.HibernateSessionService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PgBankStatementsRepository implements BankStatementsRepository {

    private final BankStatementDao bankStatementDao;

    @Inject
    public PgBankStatementsRepository(BankStatementDao bankStatementDao) {
        this.bankStatementDao = bankStatementDao;
    }

    @Override
    public BankStatement addStatementWithTransactions(BankStatement bankStatement, Collection<BankTransaction> bankTransactions) {
        bankTransactions.forEach(bankTransaction -> bankTransaction.setBankStatement(bankStatement));
        bankStatement.getBankTransactionSet().addAll(bankTransactions);
        return addBankStatement(bankStatement);
    }

    @Override
    public BankStatement addBankStatement(BankStatement bankStatement) {
        HibernateSessionService.openSession();
        bankStatementDao.create(bankStatement);
        HibernateSessionService.closeSession();
        return bankStatement;
    }

    @Override
    public Optional<BankStatement> getStatementById(int id) {
        HibernateSessionService.openSession();
        var bankStatement = bankStatementDao.findById(id);
        HibernateSessionService.closeSession();
        return bankStatement;
    }

    @Override
    public List<BankStatement> getAllStatements() {
        HibernateSessionService.openSession();
        List<BankStatement> bankStatements = HibernateSessionService.getSession()
                .createQuery("SELECT bs FROM BankStatement bs", BankStatement.class).getResultList();
        HibernateSessionService.closeSession();
        return bankStatements;
    }

    @Override
    public List<BankTransaction> getAllTransactions() {
        HibernateSessionService.openSession();
        List<BankTransaction> bankTransactions = HibernateSessionService.getSession()
                .createQuery("SELECT bt FROM BankTransaction bt", BankTransaction.class).getResultList();
        HibernateSessionService.closeSession();
        return bankTransactions;
    }

    @Override
    public void removeStatement(BankStatement bankStatement) {
        HibernateSessionService.openSession();
        bankStatementDao.remove(bankStatement);
        HibernateSessionService.closeSession();
    }

    @Override
    public void removeAllStatements() {
        Iterable<BankStatement> bankStatements = getAllStatements();
        bankStatements.forEach(this::removeStatement);
    }

}
