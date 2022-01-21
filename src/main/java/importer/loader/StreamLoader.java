package importer.loader;

import model.util.BankType;
import model.util.DocumentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StreamLoader extends AbstractLoader{
    private final InputStream stream;

    public StreamLoader(InputStream stream, String sourceDescription, BankType bankType, DocumentType documentType) {
        super(sourceDescription, bankType, documentType);
        this.stream = stream;
    }

    @Override
    public Reader load() throws IOException {
        return new InputStreamReader(stream);
    }
}