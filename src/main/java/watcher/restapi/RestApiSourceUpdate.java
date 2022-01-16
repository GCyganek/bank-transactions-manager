package watcher.restapi;

import importer.loader.StreamLoader;
import importer.loader.Loader;
import io.reactivex.rxjava3.core.Single;
import model.util.BankType;
import model.util.DocumentType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import watcher.AbstractSourceUpdate;
import watcher.restapi.response.RestUpdatesResponse;

import java.net.URL;

public class RestApiSourceUpdate extends AbstractSourceUpdate {
    private final int statementId;
    private final RestApiClient client;
    private final URL remoteUrl;

    public RestApiSourceUpdate(BankType bankType, RestApiClient client, URL remoteUrl, RestUpdatesResponse response) {
        super(bankType, DocumentType.fromString(response.getExtension()).orElseThrow());
        this.statementId = response.getStatementId();
        this.client = client;
        this.remoteUrl = remoteUrl;
    }

    @Override
    public Single<Loader> getUpdateDataLoader() {
        Call<ResponseBody> responseBodyCall = client.statementById(statementId);

        return Single.create(emitter -> responseBodyCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    emitter.onSuccess(new StreamLoader(response.body().byteStream(), remoteUrl.toString()));
                }
                else {
                    emitter.onError(new Exception("Request for " + statementId + " failed")); // TODO custom exception
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                emitter.onError(t);
            }
        }));
    }
}
