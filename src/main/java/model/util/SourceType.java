package model.util;

import java.util.Locale;
import java.util.Optional;

public enum SourceType {
    REST_API, DIRECTORY;

    public static Optional<SourceType> fromString(String repr) {
        return switch(repr.toLowerCase(Locale.ROOT)) {
            case "directory" -> Optional.of(DIRECTORY);
            case "rest_api" -> Optional.of(REST_API);
            default -> Optional.empty();
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case DIRECTORY -> "directory";
            case REST_API -> "rest_api";
        };
    }
}
