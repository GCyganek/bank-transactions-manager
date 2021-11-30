package importer.utils;

public class ParserField<K, T>{
    private final K key;
    private String parsedValue;
    private final Converter<T> converter;

    public ParserField(K key, Converter<T> converter) {
        this.key = key;
        this.converter = converter;
        parsedValue = null;
    }


    public T convert() {
        if (parsedValue == null)
            throw new IllegalArgumentException("Converting empty value");
        return converter.convert(parsedValue);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ParserField<?, ?> that) {
            return that.key.equals(key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public K getKey() {
        return key;
    }

    public void setParsedValue(String value) {
        parsedValue = value;
    }
}
