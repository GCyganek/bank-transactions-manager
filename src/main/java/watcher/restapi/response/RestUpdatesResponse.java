package watcher.restapi.response;

import com.google.gson.annotations.SerializedName;

public class RestUpdatesResponse {
    @SerializedName("statement_id")
    private int statementId;

    public int getStatementId() {
        return statementId;
    }

    public void setStatementId(int statementId) {
        this.statementId = statementId;
    }
}
