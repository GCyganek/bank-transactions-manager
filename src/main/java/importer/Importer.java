package importer;

import configurator.BankConfigurator;
import configurator.BankConfiguratorFactory;
import importer.loader.Loader;
import io.reactivex.rxjava3.core.Observable;
import model.BankStatement;
import model.BankTransaction;
import model.BankType;
import model.DocumentType;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class Importer {
    private final BankConfiguratorFactory configFactory;
    private final BankStatementsRepository repository;
    private Loader loader;
    private BankStatement importedStatement;

    @Inject
    public Importer(BankConfiguratorFactory configFactory, BankStatementsRepository repository, Loader loader) {
        this.configFactory = configFactory;
        this.loader = loader;
        this.repository = repository;
        this.importedStatement = null;
    }

    /**
     * @param bankType     - one of supported banks
     * @param documentType - file extension
     * @param URI - uri from where loader should load data
     * @return Observable that emits Bank Transactions, which have reference to imported BankStatement.
     *         Data is persisted once observable completes. Data in not persisted if any error occurs. //todo
     */
    public Observable<BankTransaction> importBankStatement(BankType bankType,
                                       DocumentType documentType, String URI) throws IOException
    {
        BankConfigurator configurator = configFactory.createBankConfigurator(bankType);
        Reader dataReader = loader.load(URI);
        BankParser<?, ?> parser = configurator.getConfiguredParser(documentType);
        return parser.parse(dataReader)
                .doOnNext(bankTransaction -> importedStatement = bankTransaction.getBankStatement())
                .doOnComplete(() -> repository.addBankStatement(importedStatement));

    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

}
