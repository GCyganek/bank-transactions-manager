package watcher;

import io.reactivex.rxjava3.core.Observable;
import model.util.BankType;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RestApiObserver implements SourceObserver {
    private final URL remoteUrl;
    private final RestApiClient client;
    private final BankType bankType;

    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public RestApiObserver(URL remoteUrl, BankType bankType) {
        this.bankType = bankType;
        this.remoteUrl = remoteUrl;
        this.client = initialize();
    }

    private RestApiClient initialize() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(remoteUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        return retrofit.create(RestApiClient.class);
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        return client.listUpdates(
                        LocalDateTime.now().minusHours(5).format(dateTimeFormatter),
                        LocalDateTime.now().format(dateTimeFormatter)
                ).flatMap(x -> Observable.fromIterable(x.getRestUpdatesResponseList())).map(RestUpdatesResponse::getStatementId)
                .map(x -> new RestApiSourceUpdate(bankType, x, client, remoteUrl));
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.REST_API;
    }
}