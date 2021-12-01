package importer.utils.converters;

public class IdentityConverter implements Converter<String> {
    @Override
    public String convert(String val) {
        return val;
    }
}
