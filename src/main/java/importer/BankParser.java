package importer;

import configurator.config.ParserConfig;
import importer.raw.RawDataParser;
import model.BankStatement;
import model.BankStatementBuilder;

import java.io.IOException;
import java.io.Reader;

public class BankParser<K, U> {
    private final RawDataParser<K, U> rawDataParser;
    private final ParserConfig<K, U> parserConfig;
    private final BankStatementBuilder<K, U> builder;

    public BankParser(ParserConfig<K, U> parserConfig,
                      BankStatementBuilder<K, U> builder) {
        this.rawDataParser = parserConfig.getRawDataParser();
        this.parserConfig = parserConfig;
        this.builder = builder;
    }

    public BankStatement parse(Reader reader) throws IOException {
        rawDataParser.parse(reader, parserConfig.getStatementFields(), parserConfig.getTransactionFields());
        builder.buildBankStatement(rawDataParser.getConvertedStatement());

        for (var convertedTransaction : rawDataParser.getConvertedTransactions()) {
            builder.buildBankTransaction(convertedTransaction);
        }

        return builder.build();
    }
}
