package importer;

import configurator.BankConfigurator;
import configurator.BankConfiguratorFactory;
import importer.loader.Loader;
import io.reactivex.rxjava3.core.Observable;
import model.BankTransaction;
import model.util.BankType;
import model.util.DocumentType;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Reader;


public class Importer {
    private final BankConfiguratorFactory configFactory;
    private Loader loader;

    @Inject
    public Importer(BankConfiguratorFactory configFactory, Loader loader) {
        this.configFactory = configFactory;
        this.loader = loader;
    }

    /**
     * @param bankType     - one of supported banks
     * @param documentType - file extension
     * @param URI - uri from where loader should load data
     * @return Observable that emits Bank Transactions, which have reference to imported BankStatement.
     */
    public Observable<BankTransaction> importBankStatement(BankType bankType,
                                       DocumentType documentType, String URI) throws IOException
    {
        BankConfigurator configurator = configFactory.createBankConfigurator(bankType);
        Reader dataReader = loader.load(URI);
        BankParser<?, ?> parser = configurator.getConfiguredParser(documentType);

        return parser.parse(dataReader)
//                .doOnNext(x -> Thread.sleep(2500)) // emulate heavy computation
                .doFinally(dataReader::close);
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

}
