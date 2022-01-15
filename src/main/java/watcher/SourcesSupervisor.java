package watcher;

import model.util.BankType;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class SourcesSupervisor {

    private final List<DataDownloader> directorySources;
    private final List<DataDownloader> remoteSources;
    private final List<DataDownloader> jeszcze_inny_source;

    public SourcesSupervisor(List<DataDownloader> directorySources, List<DataDownloader> remoteSources) {
        this.directorySources = directorySources;
        this.remoteSources = remoteSources;
    }

    public void cykliczne_sprawdzenie_czy_jest_cos_nowego() {
        // wez SourceUpdate'y od kazdego SourceObservera
        // jako ze to cykliczne sprawdzenie to tylko je zapisz na swojej liscie z updateami, bez pobierania samych danych
    }

    public List<SourceUpdate> sprawdzenie_na_polecenie() {
        // zwroc SourceUpdate'y od kazdego SourceObservera oraz te co masz zcache'owane

        // potem controller ktory to wywolal wywoluje po prostu executeUpdate() na sourceUpdate'cie
        // dostaje tym samym loadery do danych i przekazuje je do importera
        return null;
    }

    public void fetchNewStatementsFromSources() {
        directorySources.forEach(DataDownloader::importNewStatementsFromSource);
        remoteSources.forEach(DataDownloader::importNewStatementsFromSource);
    }

    /*
    ma liste DataDownloaderow dla folderow i rest api (bo sa w widoku dwie tabele i do kazdej musze podpiac dobra liste),
     metode wywołania odświeżania dla wszystkich DataDownloaderow, dodanie nowego DataDownloadera
    po dodaniu go przez uzytkownika w aplikacji i usuwanie go
     */

    public void addNewFolderSource(BankType bankType, Path path) { }

    public void addNewRemoteSource(BankType bankType, URL remoteUrl) { }

    public void deleteRemoteSource(URL remoteUrl) { }

    public void deleteDirectorySource(Path path) { }
}
