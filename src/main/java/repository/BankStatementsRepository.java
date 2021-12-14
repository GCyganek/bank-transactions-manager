package repository;

import model.BankStatement;
import model.BankTransaction;
import repository.dao.BankStatementDao;
import repository.dao.BankTransactionDao;
import session.HibernateSessionService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BankStatementsRepository {

    private final BankStatementDao bankStatementDao;
    private final BankTransactionDao bankTransactionDao;

    @Inject
    public BankStatementsRepository(BankStatementDao bankStatementDao, BankTransactionDao bankTransactionDao) {
        this.bankStatementDao = bankStatementDao;
        this.bankTransactionDao = bankTransactionDao;
    }

    public BankStatement addStatementWithTransactions(BankStatement bankStatement, Collection<BankTransaction> bankTransactions) {
        bankTransactions.forEach(bankTransaction -> bankTransaction.setBankStatement(bankStatement));
        bankStatement.getBankTransactionSet().addAll(bankTransactions);
        return addBankStatement(bankStatement);
    }

    public BankStatement addBankStatement(BankStatement bankStatement) {
        HibernateSessionService.openSession();
        bankStatementDao.create(bankStatement);
        HibernateSessionService.closeSession();
        return bankStatement;
    }

    public Optional<BankStatement> getStatementById(int id) {
        HibernateSessionService.openSession();
        var bankStatement = bankStatementDao.findById(id);
        HibernateSessionService.closeSession();
        return bankStatement;
    }

    public List<BankStatement> getAllStatements() {
        HibernateSessionService.openSession();
        List<BankStatement> bankStatements = bankStatementDao.getAllStatements();
        HibernateSessionService.closeSession();
        return bankStatements;
    }

    public List<BankTransaction> getAllTransactions() {
        HibernateSessionService.openSession();
        List<BankTransaction> bankTransactions = bankTransactionDao.getAll();
        HibernateSessionService.closeSession();
        return bankTransactions;
    }

    public void removeStatement(BankStatement bankStatement) {
        HibernateSessionService.openSession();
        bankStatementDao.remove(bankStatement);
        HibernateSessionService.closeSession();
    }

    public void removeAllStatements() {
        Iterable<BankStatement> bankStatements = getAllStatements();
        bankStatements.forEach(this::removeStatement);
    }

    public void updateTransaction(BankTransaction bankTransaction) {
        HibernateSessionService.openSession();
        bankTransactionDao.updateTransaction(bankTransaction);
        HibernateSessionService.closeSession();
    }

    public void removeTransaction(BankTransaction bankTransaction) {
        HibernateSessionService.openSession();
        bankTransactionDao.remove(bankTransaction);
        HibernateSessionService.closeSession();
    }

    public void updateStatement(BankStatement bankStatement) {
        HibernateSessionService.openSession();
        bankStatementDao.updateStatement(bankStatement);
        HibernateSessionService.closeSession();
    }
}
