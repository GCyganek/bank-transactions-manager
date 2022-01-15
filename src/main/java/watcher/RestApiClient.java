package watcher;

import io.reactivex.rxjava3.core.Observable;

// tu będzie obsluga polaczenia z serwerem, czyli pobieranie pliku/sparwdzenie, czy jest cos nowego
public class RestApiClient {
    public Observable<RestUpdatesResponse> getUpdates(remoteUrl, data_poczatu) {
        //  /api/statements/updates?start-time=20220114235600&end-time=20220115014600
        return Observable.create() {
            // get json
            // convert json
            // emit RestUpdatesResponse
        }
    }

    public Observable<Loader> getStatement(statementId) {

    }
}

/*
TODO:
    - zamiana DataDownloadera na factory co tworzy SourceObservery


    - posprzatac supervisora
    - source update dla directory i dla resta dokonczyc
    - podlaczyc SourceObservery do widoku
    - podpiecie do controllera SourceUpdateow jakos
    - implementacja rest api na retro
 */
