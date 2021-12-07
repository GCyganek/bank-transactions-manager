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

public class SantanderConfigurator extends AbstractBankConfigurator {
    private static final String statementBeginDateFormat = "yyyy-MM-dd";
    private static final String transactionDateFormat = "dd-MM-yyyy";


    public SantanderConfigurator() {
        super(BankType.SANTANDER);

        this.supportedDocumentTypes.addAll(List.of(DocumentType.CSV));
    }

    private ConfigWrapper<Cell, Integer> getCSVConfig() {
        StatementBuilderConfig<Cell> statementBuilderConfig = getCSVStatementConfig();
        TransactionBuilderConfig<Integer> transactionBuilderConfig = getCSVTransactionConfig();
        CSVRawDataParser csvRawDataParser = new CSVRawDataParser(',', 1, 1, 2);

        return new ConfigWrapper<>(csvRawDataParser, statementBuilderConfig, transactionBuilderConfig);
    }

    private TransactionBuilderConfig<Integer> getCSVTransactionConfig() {
        TransactionBuilderConfig<Integer> transactionBuilderConfig = new TransactionBuilderConfig<>();

        Converter<String> identity = new IdentityConverter();
        Converter<LocalDate> dateConverter = new DateConverter(transactionDateFormat);
        Converter<BigDecimal> FloatToBigDecimal = new FloatToBigDecimalConverter();

        transactionBuilderConfig.setDateKey(1, dateConverter);
        transactionBuilderConfig.setDescriptionKey(3, identity);
        transactionBuilderConfig.setAmountKey(6, FloatToBigDecimal);
        transactionBuilderConfig.setBalanceKey(7, FloatToBigDecimal);

        return transactionBuilderConfig;
    }

    private StatementBuilderConfig<Cell> getCSVStatementConfig() {
        StatementBuilderConfig<Cell> statementBuilderConfig = new StatementBuilderConfig<>();

        Converter<String> identity = new IdentityConverter();
        Converter<LocalDate> dateConverter = new DateConverter(transactionDateFormat);
        Converter<BigDecimal> FloatToBigDecimal = new FloatToBigDecimalConverter();

        statementBuilderConfig.setPeriodEndDateKey(new Cell(1, 1), new DateConverter(statementBeginDateFormat));
        statementBuilderConfig.setPeriodStartDateKey(new Cell(1, 2), dateConverter);
        statementBuilderConfig.setAccountNumberKey(new Cell(1, 3), identity);
        statementBuilderConfig.setAccountOwnerKey(new Cell(1, 4), identity);
        statementBuilderConfig.setCurrencyKey(new Cell(1, 5), identity);
        statementBuilderConfig.setPaidInKey(new Cell(1, 6), FloatToBigDecimal);
        statementBuilderConfig.setPaidOutKey(new Cell(1, 7), FloatToBigDecimal);

        return  statementBuilderConfig;
    }

    @Override
    protected ConfigWrapper<?, ?> getConfig(DocumentType documentType) {
        return switch (documentType) {
            case CSV -> getCSVConfig();
            default -> throw getInvalidDocumentTypeError(documentType);
        };
    }
}
