package importer.loader;

import model.util.BankType;
import model.util.DocumentType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class LocalFSLoader extends AbstractLoader {
    public LocalFSLoader(String description, BankType bankType, DocumentType documentType) {
        super(description, bankType, documentType);
    }

    @Override
    public Reader load() throws IOException {
        return new BufferedReader(new FileReader(description));
    }
}
