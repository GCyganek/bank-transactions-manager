package watcher.restapi;

import io.reactivex.rxjava3.core.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.util.BankType;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import watcher.SourceObserver;
import watcher.SourceUpdate;
import watcher.restapi.response.RestUpdatesResponse;

import java.net.ConnectException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RestApiObserver implements SourceObserver {
    private final URL remoteUrl;
    private final StringProperty remoteUrlStringProperty = new SimpleStringProperty();
    private final RestApiClient client;
    private final ObjectProperty<BankType> bankType = new SimpleObjectProperty<>();
    //TODO save last update time

    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public RestApiObserver(URL remoteUrl, BankType bankType) {
        this.bankType.set(bankType);
        this.remoteUrl = remoteUrl;
        this.remoteUrlStringProperty.set(remoteUrl.toString());
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
                        LocalDateTime.now().minusHours(5).format(dateTimeFormatter), //TODO stop using end-date parameter and ConnectException errro handling
                        LocalDateTime.now().format(dateTimeFormatter)
                ).onErrorResumeWith(Observable.empty()).flatMap(x -> Observable.fromIterable(x.getRestUpdatesResponseList())).map(RestUpdatesResponse::getStatementId)
                .map(x -> new RestApiSourceUpdate(bankType.get(), x, client, remoteUrl));
    }

    @Override
    public StringProperty descriptionProperty() {
        return remoteUrlStringProperty;
    }

    @Override
    public ObjectProperty<BankType> bankTypeProperty() { return bankType; }
}