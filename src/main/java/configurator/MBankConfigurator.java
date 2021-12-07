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

public class MBankConfigurator extends AbstractBankConfigurator{
    public MBankConfigurator() {
        super(BankType.MBANK);
        this.supportedDocumentTypes.addAll(List.of(DocumentType.CSV));
    }

    public ConfigWrapper<Cell, Integer> getCSVConfig() {
        StatementBuilderConfig<Cell> statementBuilderConfig = new StatementBuilderConfig<>();
        TransactionBuilderConfig<Integer> transactionBuilderConfig = new TransactionBuilderConfig<>();

        Converter<String> identity = new IdentityConverter();
        Converter<LocalDate> dateConverter = new DateConverter("dd.MM.yyyy");
        Converter<BigDecimal> floatToBigDecimal = new FloatToBigDecimalConverter();

        // "-3 123,464 PLN" -> "-3123.464"
        String regex = "(-?\\d+(.\\d+)?)(\\s*\\D+)?";
        Converter<BigDecimal> stripCurrencyConverter = x -> new BigDecimal(
                        x.replaceAll(" ", "")
                        .replaceAll(regex, "$1")
                        .replaceAll(",", "."));

        statementBuilderConfig.setAccountOwnerKey(new Cell(10, 1), identity);
        statementBuilderConfig.setPeriodStartDateKey(new Cell(15, 1), dateConverter);
        statementBuilderConfig.setPeriodEndDateKey(new Cell(15, 2), dateConverter);
        statementBuilderConfig.setAccountNumberKey(new Cell(19, 1), identity);
        statementBuilderConfig.setPaidInKey(new Cell(24, 2), floatToBigDecimal);
        statementBuilderConfig.setPaidOutKey(new Cell(24, 3), floatToBigDecimal);
        statementBuilderConfig.setCurrencyKey(new Cell(24, 1), identity);

        transactionBuilderConfig.setDateKey(1, dateConverter);
        transactionBuilderConfig.setDescriptionKey(2, identity);
        transactionBuilderConfig.setAmountKey(5, stripCurrencyConverter);
        transactionBuilderConfig.setBalanceKey(6, stripCurrencyConverter);

        CSVRawDataParser csvRawDataParser = new CSVRawDataParser(';', 10, 24, 27);

        return new ConfigWrapper<>(csvRawDataParser, statementBuilderConfig, transactionBuilderConfig);
    }

    @Override
    protected ConfigWrapper<?, ?> getConfig(DocumentType documentType) {
        return switch (documentType) {
            case CSV -> getCSVConfig();
            default -> throw getInvalidDocumentTypeError(documentType);
        };
    }
}
