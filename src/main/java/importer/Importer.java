package importer;

import configurator.BankConfigurator;
import configurator.BankConfiguratorFactory;
import importer.loader.Loader;
import io.reactivex.rxjava3.core.Observable;
import model.BankTransaction;
import model.util.BankType;
import model.util.DocumentType;
import repository.BankStatementsRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Reader;

public class Importer {
    private final BankConfiguratorFactory configFactory;
    private final BankStatementsRepository repository;
    private Loader loader;

    @Inject
    public Importer(BankConfiguratorFactory configFactory, BankStatementsRepository repository, Loader loader) {
        this.configFactory = configFactory;
        this.loader = loader;
        this.repository = repository;
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
                .doOnComplete(() -> repository.addBankStatement(parser.getBuiltStatement()))
                .doFinally(dataReader::close);

    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

}
