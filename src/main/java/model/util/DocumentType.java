package model.util;

import java.util.Locale;
import java.util.Optional;

public enum DocumentType {
    CSV,
    PDF;

    @Override
    public String toString() {
        return switch (this) {
            case CSV -> "CSV";
            case PDF -> "PDF";
        };
    }

    public static Optional<DocumentType> fromString(String repr) {
        String lowerRepr = repr.toLowerCase(Locale.ROOT);
        return Optional.ofNullable(switch (lowerRepr) {
            case "csv" -> DocumentType.CSV;
            case "pdf" -> DocumentType.PDF;
            default -> null;
        });
    }
}
