package importer;

import configurator.config.ParserConfig;
import importer.raw.RawDataParser;
import io.reactivex.rxjava3.core.Observable;
import model.BankStatement;
import model.BankStatementBuilder;
import model.BankTransaction;

import java.io.IOException;
import java.io.Reader;

public class BankParser<K, U>{
    private final RawDataParser<K, U> rawDataParser;
    private final ParserConfig<K, U> parserConfig;
    private final BankStatementBuilder<K, U> builder;
    private BankStatement builtStatement;

    public BankParser(ParserConfig<K, U> parserConfig,
                      BankStatementBuilder<K, U> builder) {
        this.rawDataParser = parserConfig.getRawDataParser();
        this.parserConfig = parserConfig;
        this.builder = builder;
        this.builtStatement = null;
    }

    public Observable<BankTransaction> parse(Reader reader) throws IOException {
        return rawDataParser
                .parse(reader, parserConfig.getStatementFields(), parserConfig.getTransactionFields())
                .doOnNext(parsedTransaction -> {
                    if (builtStatement == null) {
                        builtStatement = builder.buildBankStatement(rawDataParser.getConvertedStatement());
                    }})
                .flatMap(parsedTransaction -> Observable.just(builder.buildBankTransaction(parsedTransaction)))
                .doOnComplete(() -> {
                    if (builtStatement == null) {
                        builtStatement = builder.buildBankStatement(rawDataParser.getConvertedStatement());
                    }
                });
    }

    public BankStatement getBuiltStatement() {
        return builtStatement;
    }
}
