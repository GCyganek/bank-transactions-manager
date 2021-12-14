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

    public static Comparator<BankTransaction> getDateComparator() {
        return (o1, o2) -> {
            if (o1.getDate().equals(o2.getDate())) {
                if (o1.getAmount().equals(o2.getAmount())) {
                    return o1.getDescription().compareTo(o2.getDescription()) ;
                }
                return o1.getAmount().compareTo(o2.getAmount());
            }
            return  o1.getDate().compareTo(o2.getDate());
        };
    }
}
