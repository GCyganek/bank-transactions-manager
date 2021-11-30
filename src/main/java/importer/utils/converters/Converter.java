package importer.utils.converters;

@FunctionalInterface
public interface Converter<T> {
    T convert(String val);
}