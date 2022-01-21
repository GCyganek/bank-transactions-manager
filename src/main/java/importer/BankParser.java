package importer;

import importer.exceptions.ParserException;
import importer.raw.RawDataParser;
import importer.utils.ParserField;
import io.reactivex.rxjava3.core.Observable;
import model.BankStatement;
import model.BankTransaction;
import model.builder.BankStatementBuilder;
import model.builder.BankTransactionBuilder;

import java.io.Reader;
import java.util.List;

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
                .map(parsedTransaction ->
                        transactionBuilder.buildBankTransaction(getBuiltStatement(), parsedTransaction));
    }

    public BankStatement getBuiltStatement() throws ParserException {
        if (this.builtStatement == null) {
            this.builtStatement = statementBuilder.buildBankStatement(
                    rawDataParser.getConvertedStatement().orElseThrow(() ->
                            new ParserException("Transaction was emitted before statement")));
        }

        return builtStatement;
    }

    public boolean isStatementParsed() {
        return this.builtStatement != null;
    }
}
