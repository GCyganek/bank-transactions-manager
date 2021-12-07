package importer.raw;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import importer.exceptions.ParserException;
import importer.utils.Cell;
import importer.utils.ParserField;
import io.reactivex.rxjava3.core.Observable;

import java.io.Reader;
import java.util.*;

public class CSVRawDataParser implements RawDataParser<Cell, Integer> {
    private Map<Cell, ?> statementFields;
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
        this.statementFields = null;
    }

    public CSVRawDataParser() {
        this(',', 1, 1, 2);
    }


    @Override
    public Observable<Map<Integer, ?>> parse(Reader reader, List<ParserField<Cell, ?>> statementParserFields,
                                             List<ParserField<Integer, ?>> transactionParserFields)
    {
        CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder().withSeparator(separator).build());

        return Observable.create(emitter -> {
            int lineNum = 1;

            try(CSVReader csvReader = csvReaderBuilder.build()) {
                Iterator<String[]> lineIter = csvReader.iterator();

                for (; lineIter.hasNext() && !emitter.isDisposed(); lineNum++) {
                    if (lineNum == firstStatementLine) {
                        statementFields = parseStatement(lineIter, statementParserFields);
                        lineNum = lastStatementLine;
                    }
                    else if (lineNum >= firstTransactionsLine) {
                        emitter.onNext(parseTransaction(lineIter, transactionParserFields));
                    }
                    else {
                        // irrelevant data or empty space
                        lineIter.next();
                    }
                }

                if (lineNum < lastStatementLine) {
                    emitter.onError(new ParserException(
                            String.format("Statement contains fewer lines than expected. expected %d",
                                 lastStatementLine - firstStatementLine + 1)));
                    return;
                }

                emitter.onComplete();
            }
            catch (ParserException e) {
                emitter.onError(e);
            }
            catch (Exception e) {
                e.printStackTrace();
                emitter.onError(new ParserException(
                        String.format("[Parser Exception in line: %d]\t%s", lineNum, e.getMessage())));
            }
        });
    }

    private Map<Cell, Object> parseStatement(Iterator<String[]> linesIter,
                                             List<ParserField<Cell, ?>> parserFields) throws ParserException
    {
        Map<Cell, Object> result = new HashMap<>();
        ArrayList<String[]> lines = new ArrayList<>();

        // store all so parserFields won't have to be sorted by row

        int i = firstStatementLine;

        for (; i <= lastStatementLine && linesIter.hasNext(); i++) {
            lines.add(linesIter.next());
        }

        if (i < lastStatementLine) {
            throw new ParserException(
                    String.format("Statement contains fewer lines than expected. got %d expected %d",
                            i - firstStatementLine + 1, lastStatementLine - firstStatementLine + 1));
        }


        for (var field : parserFields) {
            Cell cell = field.getKey();
            field.setParsedValue(lines.get(cell.row() - firstStatementLine)[cell.col() - 1]);
            result.put(cell, field.convert());
        }

        return result;
    }


    private Map<Integer, Object> parseTransaction(Iterator<String[]> linesIter, List<ParserField<Integer, ?>> parserFields) {
        Map<Integer, Object> result = new HashMap<>();
        String[] line = linesIter.next();

        for (var field : parserFields) {
            Integer column = field.getKey();
            field.setParsedValue(line[column - 1]);
            result.put(column, field.convert());
        }

        return result;
    }

    @Override
    public Optional<Map<Cell, ?>> getConvertedStatement() {
        return Optional.ofNullable(statementFields);
    }

}
