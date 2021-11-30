package importer;

import configurator.config.ParserConfig;
import importer.raw.RawDataParser;
import model.BankStatement;
import model.BankStatementBuilder;

import java.io.IOException;
import java.io.Reader;

public class BankParser<K>{
    private RawDataParser<K> rawDataParser;
    private ParserConfig<K> parserConfig;
    private BankStatementBuilder<K> builder;

    public BankParser(RawDataParser<K> rawDataParser,
                      ParserConfig<K> parserConfig,
                      BankStatementBuilder<K> builder) {
        this.rawDataParser = rawDataParser;
        this.parserConfig = parserConfig;
        this.builder = builder;
    }

    public BankStatement parse(Reader reader) throws IOException {
        rawDataParser.parse(reader, parserConfig.getStatementFields(), parserConfig.getTransactionFields());
        builder.buildBankStatement(rawDataParser.getConvertedStatement());

        for (var convertedTransaction: rawDataParser.getConvertedTransactions()) {
            builder.buildBankTransaction(convertedTransaction);
        }

        return builder.build();
    }
}
