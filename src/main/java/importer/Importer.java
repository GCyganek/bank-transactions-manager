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

    @Inject
    public Importer(BankConfiguratorFactory configFactory) {
        this.configFactory = configFactory;
    }

    /**
     * @param bankType     - one of supported banks
     * @param documentType - file extension
     * @param loader - loader configured to load required resource
     * @return Observable that emits Bank Transactions, which have reference to imported BankStatement.
     */
    public Observable<BankTransaction> importBankStatement(BankType bankType,
                                       DocumentType documentType, Loader loader) throws IOException
    {
        BankConfigurator configurator = configFactory.createBankConfigurator(bankType);
        Reader dataReader = loader.load();
        BankParser<?, ?> parser = configurator.getConfiguredParser(documentType);

        return parser.parse(dataReader)
//                .doOnNext(x -> Thread.sleep(2500)) // emulate heavy computation
                .doFinally(dataReader::close);
    }
}
