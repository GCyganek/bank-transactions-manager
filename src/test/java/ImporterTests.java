import IOC.TestingModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import configurator.BankConfiguratorFactory;
import importer.Importer;
import importer.exceptions.ParserException;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import model.BankStatement;
import model.BankTransaction;
import model.util.BankType;
import model.util.DocumentType;
import model.util.TransactionCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import repository.BankStatementsRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


import java.io.IOException;
import java.util.stream.Stream;


public class ImporterTests {

    private static final Injector injector = Guice.createInjector(new TestingModule());

    private static final String testDirectory = "src/test/resources/";

    private final BankConfiguratorFactory configuratorFactory = injector.getInstance(BankConfiguratorFactory.class);

    @AfterEach
    public void afterEach() {
        BankStatementsRepository repository = injector.getInstance(BankStatementsRepository.class);
        repository.removeAllStatements();
    }




    @ParameterizedTest(name = "bank type={0}, doc type={1}")
    @MethodSource("correctStatementTransactionsDataSource")
    void transactionsFromCorrectlyFormattedStatementCanBeParsed(BankType bankType, DocumentType documentType, String uri,
                                                String[] expectedTransactionValues) throws IOException
    {
        TestSubscriber<String> subscriber = TestSubscriber.create();

        importAsFlowable(bankType, documentType, uri)
                .map(BankTransaction::toString)
                .subscribe(subscriber);

        subscriber.assertComplete();
        subscriber.assertValueCount(expectedTransactionValues.length);
        subscriber.assertNoErrors();
        subscriber.assertResult(expectedTransactionValues);
    }

    @ParameterizedTest(name = "bank type={0}, doc type={1}")
    @MethodSource("correctStatementStatementDataSource")
    void bankStatementFromCorrectlyFormattedStatementCanBeParsed(BankType bankType, DocumentType documentType, String uri,
                                                                String expectedValue) throws IOException
    {
        TestSubscriber<String> subscriber = TestSubscriber.create();

        importAsFlowable(bankType, documentType, uri)
            .take(1)
            .map(BankTransaction::getBankStatement)
            .map(BankStatement::toString)
            .subscribe(subscriber);

        subscriber.assertComplete();
        subscriber.assertNoErrors();
        subscriber.assertResult(expectedValue);
    }


    @ParameterizedTest(name = "bank type={0}, doc type={1}")
    @MethodSource("incorrectStatementDataSource")
    void importerThrowsParserExceptionForIncorrectlyFormattedStatement(BankType bankType,
                                                 DocumentType documentType, String uri, String expectedReason) throws IOException
    {
        TestSubscriber<BankTransaction> subscriber = TestSubscriber.create();

        importAsFlowable(bankType, documentType, uri)
                .subscribe(subscriber);

        subscriber.assertError(err -> {
            if (err instanceof ParserException e)
                return e.getReason().equals(expectedReason);
            return false;
        });
    }


    @Test
    void importerThrowsIOExceptionForInvalidSource() {
        // given
        BankType bankType = BankType.MBANK;
        DocumentType documentType = DocumentType.CSV;
        String path = "invalid_path.txt";

        // then
        assertThrows(IOException.class, () -> getImporter()
                .importBankStatement(bankType, documentType, path)
                .subscribe());
    }


    private static Stream<Arguments> correctStatementTransactionsDataSource() {
        String[] santanderCSVExpectedValues = {
                "Transaction: [(DOP. VISA 326551******6835 PŁATNOŚĆ KARTĄ 39.41 PLN JMP S.A. SKLEP 3647 WARSZAWA), (-39.41), (2021-11-24), (Uncategorized)]",
                "Transaction: [(przelew zalegly), (-5.00), (2021-11-22), (Uncategorized)]",
                "Transaction: [(Przelew środków), (824.00), (2021-11-03), (Uncategorized)]"
        };

        String[] mbankCSVExpectedValues = {
                "Transaction: [(ZAKUPY1), (-3.20), (2021-11-26), (Uncategorized)]",
                "Transaction: [(ZAKUPY2), (-9.99), (2021-11-25), (Uncategorized)]",
                "Transaction: [(ZAKUPY3), (-250.00), (2021-11-23), (Uncategorized)]"
        };

        return Stream.of(
                Arguments.of(BankType.SANTANDER, DocumentType.CSV, testDirectory + "santander_test.csv", santanderCSVExpectedValues),
                Arguments.of(BankType.MBANK, DocumentType.CSV, testDirectory + "mbank_test.csv", mbankCSVExpectedValues)
        );
    }

    // ugh
    private static Stream<Arguments> correctStatementStatementDataSource() {
        String santanderCSVExpectedValue = "Bank Statement: [(55 1245 1234 0001 0100 1244 6341), (2021-11-03), (2021-11-25), (1237.15), (948.53), (JAKUB NOWAK UL. CIEMNA 2/38 12-500 ZALNO), (PLN)]";

        String mbankCSVExpectedValue = "Bank Statement: [(eKonto - 52114020040000300276421234), (2021-11-19), (2021-11-30), (0), (-263.19), (JAN KOWALSKI), (PLN)]";

        return Stream.of(
                Arguments.of(BankType.SANTANDER, DocumentType.CSV, testDirectory + "santander_test.csv", santanderCSVExpectedValue),
                Arguments.of(BankType.MBANK, DocumentType.CSV, testDirectory + "mbank_test.csv", mbankCSVExpectedValue)
        );
    }

    private static Stream<Arguments> incorrectStatementDataSource() {
        return Stream.of(
                Arguments.of(BankType.SANTANDER, DocumentType.CSV, testDirectory + "invalid_santander_test.csv", "[Parser Exception in line: 1]\tText 'definitely not a date' could not be parsed at index 0"),
                Arguments.of(BankType.MBANK, DocumentType.CSV, testDirectory + "invalid_mbank_test.csv", "[Parser Exception in line: 10]\tText 'asd' could not be parsed at index 0"),
                Arguments.of(BankType.SANTANDER, DocumentType.CSV, testDirectory + "mbank_test.csv", "[Parser Exception in line: 1]\tText 'mBank S.A. Bankowo�� Detaliczna;;;;;' could not be parsed at index 0"),
                Arguments.of(BankType.MBANK, DocumentType.CSV, testDirectory + "santander_test.csv", "Statement contains fewer lines than expected. expected 15")
        );
    }

    private static Importer getImporter() {
        return injector.getInstance(Importer.class);
    }

    private static Flowable<BankTransaction> importAsFlowable(BankType bankType,
                                          DocumentType documentType, String uri) throws IOException
    {
            return getImporter().importBankStatement(bankType, documentType, uri)
                .toFlowable(BackpressureStrategy.BUFFER);
    }
}
