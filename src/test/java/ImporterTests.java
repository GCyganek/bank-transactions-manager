import configurator.BankConfiguratorFactory;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;


public class ImporterTests {
    BankStatementsRepository repository = new BankStatementsRepository();

    @AfterEach
    public void after() {
        repository.removeAllStatements();
    }


    private Importer getImporter() {
        BankConfiguratorFactory configuratorFactory = new BankConfiguratorFactory();
        Loader loader = new LocalFSLoader();
        return new Importer(configuratorFactory, repository, loader);
    }

    @Test
    void tempMbankImporterTest() throws URISyntaxException, IOException {
        Importer importer = getImporter();
        String uri = getMBankPath();

        importer.importBankStatement(BankType.MBANK, DocumentType.CSV, uri)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    printStatement(importer.getImportedStatement());
                    System.out.println("Size: " + repository.getAllStatements().size());
                })
                .blockingSubscribe(bankTransaction -> System.out.println("Imported Transaction: " + bankTransaction));
    }

    @Test
    void tempSantanderImporterTest() throws URISyntaxException, IOException {
        Importer importer = getImporter();
        String uri = getSantanderPath();

        importer.importBankStatement(BankType.SANTANDER, DocumentType.CSV, uri)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    printStatement(importer.getImportedStatement());
                    System.out.println("Size: " + repository.getAllStatements().size());
                })
                .blockingSubscribe(bankTransaction -> System.out.println("Imported Transaction: " + bankTransaction));
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

}
