package importer;

import configurator.BankConfigurator;
import configurator.BankConfiguratorFactory;
import importer.loader.Loader;
import io.reactivex.rxjava3.core.Observable;
import model.BankStatement;
import model.BankType;
import model.DocumentType;

import java.io.IOException;
import java.io.Reader;

public class Importer {
    private final BankConfiguratorFactory configFactory;
    private Loader loader;

    // TODO inject
    public Importer(BankConfiguratorFactory configFactory, Loader loader) {
        this.configFactory = configFactory;
        this.loader = loader;
    }

    /**
     * @param bankType  - one of supported banks
     * @param documentType - file extension
     * @param URI - uri from where loader should load data
     * @return Observable that emits single BankStatement, which have been imported and persisted in database
     */
    public Observable<BankStatement> importBankStatement(BankType bankType, DocumentType documentType, String URI) {
        return Observable.create(emitter -> {
            BankConfigurator configurator = configFactory.createBankConfigurator(bankType);
            try {
                Reader dataReader = loader.load(URI);
                BankParser<?> parser = configurator.configureParser(documentType);
                BankStatement result = parser.parse(dataReader);
                emitter.onNext(result);
                emitter.onComplete();
            } catch (IOException exception) {
                emitter.onError(exception); // TODO better error handling
            }
        });
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }
}
