import configurator.BankConfiguratorFactory;
import importer.Importer;
import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import importer.utils.converters.Converter;
import io.reactivex.rxjava3.schedulers.Schedulers;
import model.BankStatement;
import model.BankType;
import model.DocumentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import repository.BankStatementsRepository;
import repository.dao.PgBankStatementDao;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Paths;


public class ImporterTests {
    BankStatementsRepository repository = new BankStatementsRepository(new PgBankStatementDao());

    @AfterAll
    public static void afterAll() {
        BankStatementsRepository repository = new BankStatementsRepository(new PgBankStatementDao());
        repository.removeAllStatements();
    }


    private Importer getImporter() {
        BankConfiguratorFactory configuratorFactory = new BankConfiguratorFactory();
        Loader loader = new LocalFSLoader();
        return new Importer(configuratorFactory, repository, loader);
    }

    @Test
    void tempMbankImporterTest() throws URISyntaxException, IOException {
        // not really a test, see STDOUT
        Importer importer = getImporter();
        String uri = getMBankPath();

        importer.importBankStatement(BankType.MBANK, DocumentType.CSV, uri)
                .subscribeOn(Schedulers.io())
                .blockingSubscribe(bankTransaction -> System.out.println("Imported Transaction: " + bankTransaction),
                        System.out::println);
    }

    @Test
    void tempSantanderImporterTest() throws URISyntaxException, IOException {
        // not really a test, see STDOUT
        Importer importer = getImporter();
        String uri = getSantanderPath();

        importer.importBankStatement(BankType.SANTANDER, DocumentType.CSV, uri)
                .subscribeOn(Schedulers.io())
                .blockingSubscribe(bankTransaction -> System.out.println("Imported Transaction: " + bankTransaction),
                        System.out::println);

    }

    private static String getSantanderPath() throws URISyntaxException {
        return Paths.get(
                ClassLoader.getSystemResource("santander_test.csv").toURI()).toString();
    }

    private static String getMBankPath() throws URISyntaxException {
        return Paths.get(
                ClassLoader.getSystemResource("mbank_test.csv").toURI()).toString();
    }

    private static void printStatement(BankStatement statement) {
        System.out.println("==============================================");
        System.out.println(statement);
        for (var t : statement.getBankTransactionSet())
            System.out.println(t);
        System.out.println("==============================================");
    }


    @Test
    public void _x() {
        String regex = "(-?\\d+(.\\d+)?)(\\s*\\D+)?";
        Converter<BigDecimal> stripCurrencyConverter = x -> new BigDecimal(
                        x.replaceAll(" ", "")
                        .replaceAll(regex, "$1")
                        .replaceAll(",", ".")
                        );
        System.out.println(stripCurrencyConverter.convert("3 2814,65 PLN"));
    }
}
