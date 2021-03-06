package importer;

import configurator.BankConfigurator;
import configurator.BankConfiguratorFactory;
import importer.loader.Loader;
import io.reactivex.rxjava3.core.Observable;
import model.BankTransaction;

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
     * @param loader - loader configured to load required resource
     * @return Observable that emits Bank Transactions, which have reference to imported BankStatement.
     */
    public Observable<BankTransaction> importBankStatement(Loader loader) throws IOException
    {
        BankConfigurator configurator = configFactory.createBankConfigurator(loader.getBankType());
        Reader dataReader = loader.load();
        BankParser<?, ?> parser = configurator.getConfiguredParser(loader.getDocumentType());

        return parser.parse(dataReader)
                .doFinally(dataReader::close);
    }
}
