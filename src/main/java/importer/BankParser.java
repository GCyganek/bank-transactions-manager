package importer;

import importer.raw.RawDataParser;
import importer.utils.ParserField;
import io.reactivex.rxjava3.core.Observable;
import model.BankStatement;
import model.BankStatementBuilder;
import model.BankTransaction;
import model.BankTransactionBuilder;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

public class BankParser<K, U> {
    private final RawDataParser<K, U> rawDataParser;

    private final List<ParserField<K, ?>> statementFields;
    private final List<ParserField<U, ?>> transactionFields;

    private final BankStatementBuilder<K> statementBuilder;
    private final BankTransactionBuilder<U> transactionBuilder;
    private BankStatement builtStatement;

    public BankParser(RawDataParser<K, U> rawDataParser,
                      List<ParserField<K, ?>> statementFields,
                      List<ParserField<U, ?>> transactionFields,
                      BankStatementBuilder<K> statementBuilder,
                      BankTransactionBuilder<U> transactionBuilder)
    {
        this.rawDataParser = rawDataParser;

        this.statementFields = statementFields;
        this.transactionFields = transactionFields;

        this.statementBuilder = statementBuilder;
        this.transactionBuilder = transactionBuilder;
    }

    public Observable<BankTransaction> parse(Reader reader){
        return rawDataParser
                .parse(reader, statementFields, transactionFields)
                .doOnNext(parsedTransaction -> {
                    if (builtStatement == null) {
                        builtStatement = statementBuilder.buildBankStatement(rawDataParser.getConvertedStatement().get());
                    }})
                .flatMap(parsedTransaction ->
                        Observable.just(transactionBuilder.buildBankTransaction(builtStatement, parsedTransaction)))
                .doOnComplete(() -> {
                    if (builtStatement == null) {
                        builtStatement = statementBuilder.buildBankStatement(rawDataParser.getConvertedStatement().get());
                    }
                });
    }

    /**
     * @return Optional of created BankStatement,  value is guaranteed to be present after first transaction have been emitted.
     */
    public Optional<BankStatement> getBuiltStatement() {
        return Optional.ofNullable(builtStatement);
    }
}
