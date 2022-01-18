package importer.loader;

import model.util.BankType;
import model.util.DocumentType;

import java.io.*;

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