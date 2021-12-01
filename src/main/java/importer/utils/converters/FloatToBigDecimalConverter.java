package importer.utils.converters;

import java.math.BigDecimal;

public class FloatToBigDecimalConverter implements Converter<BigDecimal> {
    @Override
    public BigDecimal convert(String val) {
        return new BigDecimal(val.replace(',', '.'));
    }
}
