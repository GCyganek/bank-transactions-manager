package settings.json;

import com.google.gson.annotations.SerializedName;
import model.util.BankType;
import settings.SourceConfig;
import watcher.SourceType;

import java.time.LocalDateTime;

public class JsonSourceConfig implements SourceConfig {
    @SerializedName("description")
    private String description;

    @SerializedName("sourceType")
    private SourceType sourceType;

    @SerializedName("bankType")
    private BankType bankType;

    @SerializedName("lastUpdate")
    private LocalDateTime lastUpdateTime;

    @SerializedName("active")
    private Boolean active;

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public SourceType getSourceType() {
        return sourceType;
    }

    @Override
    public BankType getBankType() {
        return bankType;
    }

    @Override
    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public Boolean isActive() {
       return active;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public void setBankType(BankType bankType) {
        this.bankType = bankType;
    }

    @Override
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }
}
