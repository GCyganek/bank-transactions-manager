package importer.exceptions;

public class ParserException extends Exception{
    private final String reason;

    public ParserException(String reason) {
        super(String.format("Failed to parse Statement.\n %s", reason));
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
