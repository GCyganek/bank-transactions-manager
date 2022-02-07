package importer.loader;

import model.util.BankType;
import model.util.DocumentType;

import java.io.IOException;
import java.io.Reader;

public interface Loader {
    Reader load() throws IOException;
    String getDescription();

    BankType getBankType();
    DocumentType getDocumentType();
}
