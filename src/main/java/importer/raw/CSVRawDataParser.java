package importer.raw;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import importer.utils.Cell;
import importer.utils.ParserField;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class CSVRawDataParser implements RawDataParser<Cell, Integer> {
    private Map<Cell, ?> statementFields;
    private List<Map<Integer, ?>> transactionFields;
    private final char separator;

    private final int firstStatementLine, lastStatementLine, firstTransactionsLine;

    /**
     * All params should be numbered starting from 1, range is inclusive from both sides.
     */
    public CSVRawDataParser(char separator, int firstStatementLine, int lastStatementLine, int firstTransactionsLine) {
        this.separator = separator;
        this.firstStatementLine = firstStatementLine;
        this.lastStatementLine = lastStatementLine;
        this.firstTransactionsLine = firstTransactionsLine;
    }

    public CSVRawDataParser() {
        this(',',1, 1, 2);
    }


    @Override
    public void parse(Reader reader, List<ParserField<Cell, ?>> statementParserFields,
                      List<ParserField<Integer, ?>> transactionParserFields)
    {
        transactionFields = new LinkedList<>();

        CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder().withSeparator(separator).build());

        try(CSVReader csvReader = csvReaderBuilder.build()) {
            Iterator<String[]> lineIter = csvReader.iterator();

            for (int i = 1; lineIter.hasNext(); i++) {
                if (i == firstStatementLine) {
                    statementFields = parseStatement(lineIter, statementParserFields);
                    i = lastStatementLine;
                }
                else if (i >= firstTransactionsLine) {
                    transactionFields.add(parseTransaction(lineIter, transactionParserFields));
                }
                else {
                    // irrelevant data or empty space
                    lineIter.next();
                }
            }
        } catch (IndexOutOfBoundsException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to parse"); // TODO parser exception or smth
        }
    }

    private Map<Cell, Object> parseStatement(Iterator<String[]> linesIter, List<ParserField<Cell, ?>> parserFields) {
        Map<Cell, Object> result = new HashMap<>();
        ArrayList<String[]> lines = new ArrayList<>();

        // store all so parserFields won't have to be sorted by row
        for (int i=firstStatementLine; i <= lastStatementLine; i++) {
            lines.add(linesIter.next());
        }

        for (var field: parserFields) {
            Cell cell = field.getKey();
            field.setParsedValue(lines.get(cell.row - firstStatementLine)[cell.col - 1]);
            result.put(cell, field.convert());
        }

        return result;
    }


    private Map<Integer, Object> parseTransaction(Iterator<String[]> linesIter, List<ParserField<Integer, ?>> parserFields) {
        Map<Integer, Object> result = new HashMap<>();
        String[] line = linesIter.next();

        for (var field: parserFields) {
            Integer column = field.getKey();
            field.setParsedValue(line[column - 1]);
            result.put(column, field.convert());
        }

        return result;
    }

    @Override
    public Map<Cell, ?> getConvertedStatement() {
        return statementFields;
    }

    @Override
    public List<Map<Integer, ?>> getConvertedTransactions() {
        return transactionFields;
    }
}
