package watcher;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RestUpdatesResponseList {
    @SerializedName("updates")
    private List<RestUpdatesResponse> restUpdatesResponseList;

    public List<RestUpdatesResponse> getRestUpdatesResponseList() {
        return restUpdatesResponseList;
    }

    public void setRestUpdatesResponseList(List<RestUpdatesResponse> restUpdatesResponseList) {
        this.restUpdatesResponseList = restUpdatesResponseList;
    }
}
