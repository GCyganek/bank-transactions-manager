import configurator.BankConfigurator;
import configurator.BankConfiguratorFactory;
import configurator.SantanderConfigurator;
import importer.BankParser;
import importer.Importer;
import importer.loader.Loader;
import importer.loader.LocalFSLoader;
import io.reactivex.rxjava3.schedulers.Schedulers;
import model.BankStatement;
import model.BankType;
import model.DocumentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import repository.BankStatementsRepository;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class ImporterTests {
    BankStatementsRepository repository = new BankStatementsRepository();

    @AfterEach
    public void after() {
        repository.removeAllStatements();
    }

    @Test
    void tempTest() throws URISyntaxException, IOException {
        BankConfigurator configurator = new SantanderConfigurator(repository);
        BankParser<?> parser = configurator.configureParser(DocumentType.CSV);
        Loader loader = new LocalFSLoader();
        BankStatement result = parser.parse(loader.load(getSantanderPath()));
        printStatement(result);
    }


    @Test
    void tempImporterTest() throws URISyntaxException {
        Importer importer = new Importer(new BankConfiguratorFactory(repository), new LocalFSLoader());
        String uri = getSantanderPath();
        importer.importBankStatement(BankType.SANTANDER, DocumentType.CSV, uri)
                .subscribeOn(Schedulers.io()) // there is more io type of operations here (reading file, saving to database)
                .blockingSubscribe(ImporterTests::printStatement);
    }

    private static String getSantanderPath() throws URISyntaxException {
       return Paths.get(
               ClassLoader.getSystemResource("santander_test.csv").toURI()).toString();
    }

    private static void printStatement(BankStatement statement) {
        System.out.println(statement);
        for (var t: statement.getBankTransactionSet())
            System.out.println(t);
    }
}
