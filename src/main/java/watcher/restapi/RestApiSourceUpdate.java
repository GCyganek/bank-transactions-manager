package watcher.restapi;

import importer.loader.ByteLoader;
import importer.loader.Loader;
import io.reactivex.rxjava3.core.Single;
import model.util.BankType;
import model.util.DocumentType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import watcher.AbstractSourceUpdate;

import java.io.IOException;
import java.net.URL;

public class RestApiSourceUpdate extends AbstractSourceUpdate {
    private final int statementId;
    private final RestApiClient client;
    private final URL remoteUrl;

    public RestApiSourceUpdate(BankType bankType,
                               int statementId, RestApiClient client, URL remoteUrl) {
        super(bankType);
        this.statementId = statementId;
        this.client = client;
        this.remoteUrl = remoteUrl;
    }

    @Override
    public Single<Loader> executeUpdate() {
        this.documentType = DocumentType.CSV; //TODO
        Call<ResponseBody> responseBodyCall = client.statementById(statementId);

        return Single.create(emitter -> responseBodyCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        emitter.onSuccess(new ByteLoader(response.body().bytes(), remoteUrl.toString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println(t.getMessage() + " " + t.getCause());
            }
        }));
    }
}
