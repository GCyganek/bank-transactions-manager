import model.BankStatement;
import model.BankTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import repository.BankStatementsRepository;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTests {
    private final BankStatementsRepository bankStatementsRepository = new BankStatementsRepository();

    private final BankTransaction bankTransactionExample1 =
            new BankTransaction("Przelew 1", new BigDecimal("2512.23"),
                    LocalDate.of(2021, 11, 22), new BigDecimal("2990.00"));

    private final BankTransaction bankTransactionExample2 =
            new BankTransaction("Przelew 2", new BigDecimal("23.23"),
                    LocalDate.of(2021, 11, 23), new BigDecimal("2332.00"));

    private final BankTransaction bankTransactionExample3 =
            new BankTransaction("Przelew 3", new BigDecimal("2512.23"),
                    LocalDate.of(2021, 11, 22), new BigDecimal("2990.00"));

    private final BankTransaction bankTransactionExample4 =
            new BankTransaction("Przelew 4", new BigDecimal("23.23"),
                    LocalDate.of(2021, 11, 23), new BigDecimal("2332.00"));

    private final BankStatement bankStatementExample1 =
            new BankStatement("1234 1234 1234 1234", LocalDate.of(2021, 11, 21),
                    LocalDate.of(2021, 11, 25), new BigDecimal("250.22"),
                    new BigDecimal("302.10"), "Jan Kowalski");

    private final BankStatement bankStatementExample2 =
            new BankStatement("1234 4321 4312 1234", LocalDate.of(2021, 11, 20),
                    LocalDate.of(2021, 11, 26), new BigDecimal("2453.22"),
                    new BigDecimal("332.10"), "Anna Kowalska");

    @AfterEach
    public void after() {
        bankStatementsRepository.removeAllStatements();
    }

    @Test
    public void addBankStatementsToRepositoryTest() {
        // When
        bankStatementsRepository.addStatementWithTransactions(bankStatementExample1, List.of());
        bankStatementsRepository.addStatementWithTransactions(bankStatementExample2, List.of());

        //Then
        List<BankStatement> bankStatements = bankStatementsRepository.getAllStatements();
        assertEquals(2, bankStatements.size());
        assertTrue(bankStatements.containsAll(List.of(bankStatementExample1, bankStatementExample2)));
    }

    @Test
    public void addBankStatementWithTransactionsToRepositoryTest() {
        // When
        List<BankTransaction> bankTransactions = List.of(bankTransactionExample1, bankTransactionExample2);

        bankStatementsRepository.addStatementWithTransactions(bankStatementExample1, bankTransactions);

        //Then
        List<BankStatement> bankStatements = bankStatementsRepository.getAllStatements();
        assertEquals(1, bankStatements.size());
        assertTrue(bankStatements.contains(bankStatementExample1));
        assertTrue(bankStatements.get(0).getBankTransactionSet().containsAll(bankTransactions));
    }

    @Test
    public void removeStatementFromRepositoryTest() {
        // When
        List<BankTransaction> bankTransactions = List.of(bankTransactionExample1, bankTransactionExample2);

        var bankStatement = bankStatementsRepository.addStatementWithTransactions(bankStatementExample1, bankTransactions);

        //Then
        var id = bankStatement.getId();
        bankStatementsRepository.removeStatement(bankStatement);
        var bankStatementById = bankStatementsRepository.getStatementById(id);
        assertFalse(bankStatementById.isPresent());
        assertEquals(0, bankStatementsRepository.getAllTransactions().size());
    }

    @Test
    public void removeAllBankStatementsFromRepositoryTest() {
        // When
        var bankStatement1 = bankStatementsRepository.addStatementWithTransactions(bankStatementExample1, List.of());
        var bankStatement2 = bankStatementsRepository.addStatementWithTransactions(bankStatementExample2, List.of());

        //Then
        var id1 = bankStatement1.getId();
        var id2 = bankStatement2.getId();

        bankStatementsRepository.removeAllStatements();

        var bankStatementById1 = bankStatementsRepository.getStatementById(id1);
        assertFalse(bankStatementById1.isPresent());

        var bankStatementById2 = bankStatementsRepository.getStatementById(id2);
        assertFalse(bankStatementById2.isPresent());

        assertEquals(0, bankStatementsRepository.getAllStatements().size());
    }

}
