package importer.extractors;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface ExtractorStrategy {
    /**
     *
     * Extracts Statement InputStream and InputStream with all transactions from
     * InputStream with both these sub-streams. Original stream is closed after operation.
     *
     * @param inputStream stream from which data should be extracted
     */
    @Deprecated(since = "might be replaced with some kind of preprocessor or idk")
    void extract(InputStream inputStream) throws IOException;

    InputStream getExtractedStatementStream();
    InputStream getExtractedTransactionsStream();
}
