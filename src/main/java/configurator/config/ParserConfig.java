package configurator.config;
import importer.utils.ParserField;

import java.util.List;

public interface ParserConfig<K> {
    List<ParserField<K, ?>> getStatementFields();
    List<ParserField<K, ?>> getTransactionFields();
}