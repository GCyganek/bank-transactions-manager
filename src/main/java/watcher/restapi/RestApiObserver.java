package watcher.restapi;

import io.reactivex.rxjava3.core.Observable;
import model.util.BankType;
import model.util.DocumentType;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import watcher.AbstractSourceObserver;
import watcher.SourceType;
import watcher.SourceUpdate;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RestApiObserver extends AbstractSourceObserver {
    private static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";

    private final URL remoteUrl;
    private final RestApiClient client;
    private LocalDateTime lastUpdate = LocalDateTime.now();

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    public RestApiObserver(URL remoteUrl, BankType bankType) {
        super(remoteUrl.toString(), bankType, SourceType.REST_API);
        this.remoteUrl = remoteUrl;

        this.client = initializeRestClient();
    }

    private RestApiClient initializeRestClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(remoteUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        return retrofit.create(RestApiClient.class);
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        String lastUpdateString = lastUpdate.format(dateTimeFormatter);
        lastUpdate = LocalDateTime.now();
        return client.listUpdates(lastUpdateString)
                .doOnError(sourceFailedPublisher::onNext)
                .onErrorResumeWith(Observable.empty())
                .flatMap(updateList -> Observable.fromIterable(updateList.getRestUpdatesResponseList()))
                .filter(response -> DocumentType.fromString(response.getExtension()).isPresent())
                .map(response -> new RestApiSourceUpdate(bankType.get(), client, remoteUrl, response));
    }
}