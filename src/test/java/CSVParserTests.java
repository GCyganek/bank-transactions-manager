import importer.Importer;
import importer.raw.CSVRawDataParser;
import importer.utils.Cell;
import importer.utils.ParserField;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;



public class CSVParserTests {

//    @Test
//    public void validFileCanBeParsed() {
//        // given
//        Reader reader = new StringReader("123,\"abcx\"\n\"321\",7");
//        ParserField<Cell, Integer> numberField = new ParserField<>(new Cell(1, 1));
//        ParserField<Cell, String> stringField = (ParserField<Cell, String >) mock(ParserField.class);
//
//
//
//        ParserField<Integer, String> transactionStringField = (ParserField<Integer, String>) mock(ParserField.class);
//        ParserField<Integer, Integer> transactionNumberField = (ParserField<Integer, Integer>) mock(ParserField.class);
//
//        given(transactionNumberField.getKey()).willReturn(1);
//        given(transactionStringField.getKey()).willReturn(2);
//
//        List<ParserField<Cell, ?>> statementFields = List.of(numberField, stringField);
//        List<ParserField<Integer, ?>> transactionFields = List.of(transactionNumberField, transactionStringField);
//
//        CSVRawDataParser csvRawDataParser = new CSVRawDataParser(',', 1, 1, 2);
//
//
//        // when
//        Observable<Map<Integer, ?>> observable = csvRawDataParser.parse(reader, statementFields, transactionFields);
//
//        // then
//        observable
//                .test()
//                .assertValueCount(1)
//                .assertNoErrors()
//                .assertValue(map -> map.get(transactionNumberField.getKey()).equals(7))
//                .assertValue(map -> map.get(transactionStringField.getKey()).equals("321"));
////                .assertValue(map -> csvRawDataParser.getConvertedStatement().get(numberField.getKey()).equals(123))
////                .assertValue(map -> csvRawDataParser.getConvertedStatement().get(stringField.getKey()).equals("abc"));
//    }

}
