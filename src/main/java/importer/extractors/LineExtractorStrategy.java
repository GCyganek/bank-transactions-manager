package importer.extractors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Scanner;

public class LineExtractorStrategy implements ExtractorStrategy {
    private final int lastStatementLine;
    private InputStream statementStream, transactionsStream;
    private String lineDelimiter;

    public LineExtractorStrategy(int lastStatementLine) {
        this(lastStatementLine, "\n");
    }

    public LineExtractorStrategy(int lastStatementLine, String lineDelimiter) {
        this.lastStatementLine = lastStatementLine;
        this.lineDelimiter = lineDelimiter;
        statementStream = null;
        transactionsStream = null;
    }


    @Override
    public void extract(InputStream inputStream) throws IOException {
        Scanner lineScanner = new Scanner(inputStream);
        lineScanner.useDelimiter(lineDelimiter);

        LinkedList<String> statementData = new LinkedList<>();

        for (int i=0; i<lastStatementLine; i++) {
            if (!lineScanner.hasNext())
                throw new RuntimeException("invalid input"); // TODO

            statementData.add(lineScanner.nextLine());
            statementData.add(lineDelimiter);
        }

        statementStream = getInputStream(statementData);

        LinkedList<String> transactionsData = new LinkedList<>();

        while (lineScanner.hasNext()){
            transactionsData.add(lineScanner.nextLine());
            transactionsData.add(lineDelimiter);
        }

        transactionsStream = getInputStream(transactionsData);
        inputStream.close();
    }

    private InputStream getInputStream(LinkedList<String> lines) {
        byte[] bytes = lines.stream()
                .reduce("", (line, acc) -> line + acc)
                .getBytes(StandardCharsets.UTF_8);

        return new ByteArrayInputStream(bytes);
    }

    @Override
    public InputStream getExtractedStatementStream() {
        return statementStream; // maybe should check if it was parsed before
    }

    @Override
    public InputStream getExtractedTransactionsStream() {
        return transactionsStream;
    }
}
