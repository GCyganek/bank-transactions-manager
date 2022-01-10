package model.util;

public enum BankType {
    SANTANDER,
    MBANK;

    @Override
    public String toString() {
        return switch (this) {
            case SANTANDER -> "Santander";
            case MBANK -> "MBank";
        };
    }
}
