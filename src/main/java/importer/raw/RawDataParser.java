package importer.raw;

import importer.utils.ParserField;

import java.io.Reader;
import java.util.List;
import java.util.Map;

public interface RawDataParser<K, U> {
    void parse(Reader reader, List<ParserField<K, ?>> StatementParserFields,
               List<ParserField<U, ?>> TransactionParserFields);

    Map<K, ?> getConvertedStatement();
    List<Map<U, ?>> getConvertedTransactions();
}
