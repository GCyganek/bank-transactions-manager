package model;

public enum DocumentType {
    CSV;

    @Override
    public String toString() {
        return switch (this) {
            case CSV -> "CSV";
        };
    }
}
