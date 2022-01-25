package model.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import model.BankTransaction;

import java.util.Comparator;

public final class ModelUtil {

    public static boolean propertyEquals(StringProperty property1, StringProperty property2) {
        if (property1.get() == null) return false;
        return property1.get().equals(property2.get());
    }

    public static <T> boolean propertyEquals(ObjectProperty<T> property1, ObjectProperty<T> property2) {
        if (property1.get() == null) return false;
        return property1.get().equals(property2.get());
    }

    public static Comparator<BankTransaction> getDateThenAmountThenDescriptionComparator() {
        return Comparator.comparing(BankTransaction::getDate)
                .thenComparing(BankTransaction::getAmount)
                .thenComparing(BankTransaction::getDescription)
                .thenComparing(BankTransaction::getCategory);
    }
}
