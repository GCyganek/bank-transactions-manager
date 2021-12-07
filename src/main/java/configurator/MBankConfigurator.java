package configurator;

import configurator.config.util.ConfigWrapper;
import configurator.config.StatementBuilderConfig;
import configurator.config.TransactionBuilderConfig;
import importer.raw.CSVRawDataParser;
import importer.utils.Cell;
import importer.utils.converters.Converter;
import importer.utils.converters.DateConverter;
import importer.utils.converters.FloatToBigDecimalConverter;
import importer.utils.converters.IdentityConverter;
import model.BankType;
import model.DocumentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class MBankConfigurator extends AbstractBankConfigurator{
    private static final String dateFormat = "dd.MM.yyyy";
    private static final String stripCurrencyRegex = "(-?\\d+(\\.\\d+)?)(\\s*\\D+)?";

    private final Pattern stripCurrencyPattern;

    public MBankConfigurator() {
        super(BankType.MBANK);
        this.supportedDocumentTypes.addAll(List.of(DocumentType.CSV));
        stripCurrencyPattern = Pattern.compile(stripCurrencyRegex);
    }

    public ConfigWrapper<Cell, Integer> getCSVConfig() {
        StatementBuilderConfig<Cell> statementBuilderConfig = getCSVStatementConfig();
        TransactionBuilderConfig<Integer> transactionBuilderConfig = getCSVTransactionConfig();
        CSVRawDataParser csvRawDataParser = new CSVRawDataParser(';', 10, 24, 27);

        return new ConfigWrapper<>(csvRawDataParser, statementBuilderConfig, transactionBuilderConfig);
    }

    private StatementBuilderConfig<Cell> getCSVStatementConfig() {
        StatementBuilderConfig<Cell> statementBuilderConfig = new StatementBuilderConfig<>();

        Converter<String> identity = new IdentityConverter();
        Converter<LocalDate> dateConverter = new DateConverter(dateFormat);
        Converter<BigDecimal> floatToBigDecimal = new FloatToBigDecimalConverter();

        statementBuilderConfig.setAccountOwnerKey(new Cell(10, 1), identity);
        statementBuilderConfig.setPeriodStartDateKey(new Cell(15, 1), dateConverter);
        statementBuilderConfig.setPeriodEndDateKey(new Cell(15, 2), dateConverter);
        statementBuilderConfig.setAccountNumberKey(new Cell(19, 1), identity);
        statementBuilderConfig.setPaidInKey(new Cell(24, 2), floatToBigDecimal);
        statementBuilderConfig.setPaidOutKey(new Cell(24, 3), floatToBigDecimal);
        statementBuilderConfig.setCurrencyKey(new Cell(24, 1), identity);

        return statementBuilderConfig;
    }

    private TransactionBuilderConfig<Integer> getCSVTransactionConfig() {
        TransactionBuilderConfig<Integer> transactionBuilderConfig = new TransactionBuilderConfig<>();

        Converter<String> identity = new IdentityConverter();
        Converter<LocalDate> dateConverter = new DateConverter(dateFormat);

        // "-3 123,464 PLN" -> "-3123.464"
        Converter<BigDecimal> stripCurrencyConverter = x -> {
            x = x.replaceAll(" ", "").replace(",", ".");
            return new BigDecimal(stripCurrencyPattern.matcher(x).replaceAll("$1"));
        };

        transactionBuilderConfig.setDateKey(1, dateConverter);
        transactionBuilderConfig.setDescriptionKey(2, identity);
        transactionBuilderConfig.setAmountKey(5, stripCurrencyConverter);
        transactionBuilderConfig.setBalanceKey(6, stripCurrencyConverter);

        return transactionBuilderConfig;
    }

    @Override
    protected ConfigWrapper<?, ?> getConfig(DocumentType documentType) {
        return switch (documentType) {
            case CSV -> getCSVConfig();
            default -> throw getInvalidDocumentTypeError(documentType);
        };
    }
}
