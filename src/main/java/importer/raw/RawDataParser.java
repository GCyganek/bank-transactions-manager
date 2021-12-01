package importer.raw;

import importer.utils.ParserField;
import io.reactivex.rxjava3.core.Observable;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RawDataParser<K, U> {

    /**
     * Field's key has to be hashable and should allow parser to map underlying data to ParserField.
     * Field's converter has to allow conversion from string to whatever type underlying data is.
     * Reader is closed before observable terminates.
     *
     * @return Observable of map containing parsed transactions, i.e.
     *         key of given parserField will map to value obtained from converter provided in that field.
     *
     *         ParserException with appropriate reason is emitted on failure.
     */
    Observable<Map<U, ?>> parse(Reader reader, List<ParserField<K, ?>> StatementParserFields,
                                List<ParserField<U, ?>> TransactionParserFields);


    /**
     * @return Optional of parsedStatement, value is guaranteed to be present after first transaction have been emitted.
     */
    Optional<Map<K, ?>> getConvertedStatement();
}
