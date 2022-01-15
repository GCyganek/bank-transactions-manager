package watcher;

import model.util.BankType;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class SourcesSupervisor {

    private final List<DataDownloader> directorySources;
    private final List<DataDownloader> remoteSources;

    public SourcesSupervisor(List<DataDownloader> directorySources, List<DataDownloader> remoteSources) {
        this.directorySources = directorySources;
        this.remoteSources = remoteSources;
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
