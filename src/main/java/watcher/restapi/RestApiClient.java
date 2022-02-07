package watcher.restapi;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import watcher.restapi.response.RestUpdatesResponseList;

public interface RestApiClient {

    @GET("api/statements/updates")
    Observable<RestUpdatesResponseList> listUpdates(@Query("start-time") String startTime);

    @GET("api/statements/{statement-id}")
    Call<ResponseBody> statementById(@Path("statement-id") int statementId);
}
