package model;

public enum TransactionCategory {
    UNCATEGORIZED,
    HEALTH_AND_BEAUTY,
    FOOD,
    CLOTHES_AND_SHOES;

    @Override
    public String toString() {
        return switch(this) {
            case UNCATEGORIZED -> "Uncategorized";
            case HEALTH_AND_BEAUTY -> "Health and beauty";
            case FOOD -> "Food";
            case CLOTHES_AND_SHOES -> "Clothes and shoes";
        };
    }
}
