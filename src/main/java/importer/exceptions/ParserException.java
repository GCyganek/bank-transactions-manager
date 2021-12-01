package importer.exceptions;

public class ParserException extends Exception{
    private final String reason;

    public ParserException(String reason) {
        super("Failed to parse Statement.\nPlease make sure that correct bank type is selected.\n");
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
