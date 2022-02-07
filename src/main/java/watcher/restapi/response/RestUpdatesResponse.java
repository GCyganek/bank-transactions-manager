package watcher.restapi.response;

import com.google.gson.annotations.SerializedName;

public class RestUpdatesResponse {
    @SerializedName("statement_id")
    private int statementId;

    @SerializedName("extension")
    private String extension;

    public int getStatementId() {
        return statementId;
    }

    public void setStatementId(int statementId) {
        this.statementId = statementId;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
