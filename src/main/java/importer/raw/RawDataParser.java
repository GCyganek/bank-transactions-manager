package importer.raw;

import importer.utils.ParserField;
import io.reactivex.rxjava3.core.Observable;

import java.io.Reader;
import java.util.List;
import java.util.Map;

public interface RawDataParser<K, U> {
    Observable<Map<U, ?>> parse(Reader reader, List<ParserField<K, ?>> StatementParserFields,
                                List<ParserField<U, ?>> TransactionParserFields);

    Map<K, ?> getConvertedStatement();
}
