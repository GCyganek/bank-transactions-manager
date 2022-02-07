package settings;

import model.util.BankType;
import model.util.SourceType;

import java.time.LocalDateTime;

public interface SourceConfig {
    String getDescription();
    BankType getBankType();
    SourceType getSourceType();
    LocalDateTime getLastUpdateTime();
    Boolean isActive();

    void setDescription(String description);
    void setBankType(BankType bankType);
    void setSourceType(SourceType sourceType);
    void setLastUpdateTime(LocalDateTime lastUpdateTime);
    void setActive(Boolean active);
}
