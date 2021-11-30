package importer.utils;

@FunctionalInterface
public interface Converter<T> {
    T convert(String val);
}