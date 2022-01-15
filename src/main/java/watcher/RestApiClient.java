package watcher;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RestApiClient {

    @GET("api/statements/updates")
    Observable<RestUpdatesResponseList> listUpdates(@Query("start-time") String startTime, @Query("end-time") String endTime);

    @GET("api/statements/{statement-id}")
    Call<ResponseBody> statementById(@Path("statement-id") int statementId);
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
