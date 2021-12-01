package configurator.config;

import importer.raw.RawDataParser;
import importer.utils.ParserField;

import java.util.List;

public interface ParserConfig<K, U> {
    List<ParserField<K, ?>> getStatementFields();

    List<ParserField<U, ?>> getTransactionFields();

    RawDataParser<K, U> getRawDataParser();
}