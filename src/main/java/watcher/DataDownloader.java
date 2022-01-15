package watcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class DataDownloader {
    /*
    Ma swojego Observera (DirectoryObserver lub RestApiObserver), ktory zwraca mu nowe pliki przy imporcie cyklicznym/
    na żądanie. Ma wszystkie dane potrzebne do tego, żeby dla każdego pliku otrzymywanego (BankType, jaki rodzaj źródła, path/url, itp)
     z Observera wywołać na nim import
     */

    private final SourceObserver sourceObserver;

    public DataDownloader(String sourceUri, SourceType sourceType) {
        this.sourceObserver = initializeSourceObserver(sourceUri, sourceType);
    }

    private SourceObserver initializeSourceObserver(String sourceUri, SourceType sourceType) {
        switch (sourceType) {
            case REST_API -> {
                try {
                    return new RestApiObserver(new URL(sourceUri));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            case DIRECTORY -> {
                try {
                    return new DirectoryObserver(Path.of(sourceUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            default -> System.out.println("error"); //TODO
        }
    }

    public void importNewStatementsFromSource() {
        sourceObserver.getChanges().subscribe(); //TODO
    }
}
