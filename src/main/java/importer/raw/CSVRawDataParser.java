package importer.raw;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import importer.raw.RawDataParser;
import importer.utils.ParserField;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CSVRawDataParser implements RawDataParser<Integer> {
    private Map<Integer, ?> statementFields;
    private List<Map<Integer, ?>> transactionFields;

    @Override
    public void parse(Reader reader, List<ParserField<Integer, ?>> statementParserFields,
                      List<ParserField<Integer, ?>> transactionParserFields)
    {
        transactionFields = new LinkedList<>();

        try(CSVReader csvReader = new CSVReader(reader)) {
            String[] statementLine = csvReader.readNext();
            statementFields = processLine(statementParserFields, statementLine);

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                transactionFields.add(processLine(transactionParserFields, line));
            }
        } catch (CsvValidationException | IndexOutOfBoundsException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to parse"); // TODO parser exception or smth
        }
    }

    private Map<Integer, Object> processLine(List<ParserField<Integer, ?>> parserFields, String[] line) {
        Map<Integer, Object> result = new HashMap<>();

        for (var field: parserFields) {
            Integer column = field.getKey();
            field.setParsedValue(line[column]);
            result.put(column, field.convert());
        }

        return result;
    }

    @Override
    public Map<Integer, ?> getConvertedStatement() {
        return statementFields;
    }

    @Override
    public List<Map<Integer, ?>> getConvertedTransactions() {
        return transactionFields;
    }
}
