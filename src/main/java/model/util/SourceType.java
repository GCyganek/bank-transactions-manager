package model.util;

import java.util.Locale;
import java.util.Optional;

public enum SourceType {
    REST_API, DIRECTORY;

    public static Optional<SourceType> fromString(String repr) {
        SourceType sourceType = switch(repr.toLowerCase(Locale.ROOT)) {
            case "directory" -> DIRECTORY;
            case "rest_api" -> REST_API;
            default -> null;
        };

        return Optional.ofNullable(sourceType);
    }

    @Override
    public String toString() {
        return switch (this) {
            case DIRECTORY -> "directory";
            case REST_API -> "rest_api";
        };
    }
}
