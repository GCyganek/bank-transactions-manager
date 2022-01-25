package watcher.restapi;

import io.reactivex.rxjava3.core.Observable;
import model.util.BankType;
import model.util.DocumentType;
import model.util.SourceType;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import watcher.AbstractSourceObserver;
import watcher.SourceUpdate;
import watcher.exceptions.InvalidSourceConfigException;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RestApiObserver extends AbstractSourceObserver {
    private static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";

    private final URL remoteUrl;
    private final RestApiClient client;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    public RestApiObserver(URL remoteUrl, BankType bankType,
                           LocalDateTime lastUpdateTime, boolean isActive) throws InvalidSourceConfigException{
        super(remoteUrl.toString(), bankType, SourceType.REST_API, lastUpdateTime, isActive);
        this.remoteUrl = remoteUrl;

        this.client = initializeRestClient();
    }

    private RestApiClient initializeRestClient() throws InvalidSourceConfigException {
        try {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(remoteUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();

            return retrofit.create(RestApiClient.class);
        } catch (Exception e) {
            sourceFailedPublisher.onNext(e);
            throw new InvalidSourceConfigException(e.getMessage());
        }
    }

    public URL getRemoteUrl() {
        return remoteUrl;
    }

    public RestApiClient getClient() {
        return client;
    }

    @Override
    public Observable<SourceUpdate> getChanges() {
        String lastUpdateString = lastUpdateCheckTime.format(dateTimeFormatter);
        lastUpdateCheckTime = LocalDateTime.now();

        return client.listUpdates(lastUpdateString)
                .doOnError(sourceFailedPublisher::onNext)
                .onErrorResumeWith(Observable.empty())
                .flatMap(updateList -> Observable.fromIterable(updateList.getRestUpdatesResponseList()))
                .filter(response -> DocumentType.fromString(response.getExtension()).isPresent())
                .map(response -> new RestApiSourceUpdate(this, response, lastUpdateCheckTime));
    }
}