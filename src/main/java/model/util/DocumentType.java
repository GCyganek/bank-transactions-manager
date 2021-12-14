package model.util;

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
}
