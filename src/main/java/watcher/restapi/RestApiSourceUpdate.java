package watcher.restapi;

import importer.loader.Loader;
import importer.loader.StreamLoader;
import io.reactivex.rxjava3.core.Single;
import model.util.DocumentType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import watcher.AbstractSourceUpdate;
import watcher.exceptions.StatementRemoteRequestFailedException;
import watcher.restapi.response.RestUpdatesResponse;

import java.net.URL;
import java.time.LocalDateTime;

public class RestApiSourceUpdate extends AbstractSourceUpdate {
    private final int statementId;
    private final RestApiClient client;
    private final URL remoteUrl;

    public RestApiSourceUpdate(RestApiObserver restApiObserver, RestUpdatesResponse response, LocalDateTime updateCheckTime) {
        super(restApiObserver, DocumentType.fromString(response.getExtension()).orElseThrow(), updateCheckTime);
        this.statementId = response.getStatementId();
        this.client = restApiObserver.getClient();
        this.remoteUrl = restApiObserver.getRemoteUrl();
    }

    @Override
    public Single<Loader> getUpdateDataLoader() {
        Call<ResponseBody> responseBodyCall = client.statementById(statementId);

        return Single.create(emitter -> responseBodyCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    emitter.onSuccess(new StreamLoader(response.body().byteStream(), remoteUrl.toString(), bankType, documentType));
                }
                else {
                    emitter.onError(new StatementRemoteRequestFailedException("Request for " + statementId + " failed"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                emitter.onError(t);
            }
        }));
    }
}
