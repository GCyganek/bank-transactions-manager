package repository;

import model.BankStatement;
import model.BankTransaction;
import repository.dao.BankStatementDao;
import session.HibernateSessionService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BankStatementsRepository {

    private final BankStatementDao bankStatementDao;

    @Inject
    public BankStatementsRepository(BankStatementDao bankStatementDao) {
        this.bankStatementDao = bankStatementDao;
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

        List<BankStatement> bankStatements = getAllStatements();

        List<BankTransaction> bankTransactions = new ArrayList<>();
        for (BankStatement bankStatement : bankStatements) {
            var bankStatementId = bankStatement.getId();
            var bankStatementTransactions = bankStatementDao.getAllTransactionsFromStatement(bankStatementId);
            bankTransactions.addAll(bankStatementTransactions);
        }

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

}
