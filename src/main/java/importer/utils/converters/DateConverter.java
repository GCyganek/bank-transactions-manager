package importer.utils.converters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateConverter implements Converter<LocalDate> {
    private final DateTimeFormatter formatter;

    public DateConverter(String format) {
        formatter = DateTimeFormatter.ofPattern(format);
    }

    @Override
    public LocalDate convert(String val) {
        return LocalDate.parse(val, formatter);
    }
}
