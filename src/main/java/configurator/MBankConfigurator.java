package configurator;

import configurator.config.Config;
import configurator.config.StatementConfig;
import configurator.config.TransactionConfig;
import importer.BankParser;
import importer.raw.CSVRawDataParser;
import importer.utils.Cell;
import importer.utils.converters.Converter;
import importer.utils.converters.DateConverter;
import importer.utils.converters.FloatToBigDecimalConverter;
import importer.utils.converters.IdentityConverter;
import model.BankType;
import model.DocumentType;
import repository.BankStatementsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MBankConfigurator extends AbstractBankConfigurator{
    public MBankConfigurator(BankStatementsRepository repository) {
        super(BankType.MBANK, repository);

        this.supportedDocumentTypes.addAll(List.of(DocumentType.CSV));
    }

    public Config<Cell, Integer> getCSVConfig() {
        StatementConfig<Cell> statementConfig = new StatementConfig<>();
        TransactionConfig<Integer> transactionConfig = new TransactionConfig<>();

        Converter<String> identity = new IdentityConverter();
        Converter<LocalDate> dateConverter = new DateConverter("dd.MM.yyyy");
        Converter<BigDecimal> floatToBigDecimal = new FloatToBigDecimalConverter();

        // "-123,464 PLN" -> "-123.464"
        String regex ="(-?\\d+(.\\d+)?)(\\s*\\D+)?";
        Converter<BigDecimal> stripCurrencyConverter = x -> new BigDecimal(
                x.replaceAll(",", ".")
                .replaceAll(regex, "$1"));

        statementConfig.setAccountOwnerKey(new Cell(10, 1), identity);
        statementConfig.setPeriodStartDateKey(new Cell(15, 1), dateConverter);
        statementConfig.setPeriodEndDateKey(new Cell(15, 2), dateConverter);
        statementConfig.setAccountNumberKey(new Cell(19, 1), identity);
        statementConfig.setPaidInKey(new Cell(24, 2), floatToBigDecimal);
        statementConfig.setPaidOutKey(new Cell(24, 3), floatToBigDecimal);

        transactionConfig.setDateKey(1, dateConverter);
        transactionConfig.setDescriptionKey(2, identity);
        transactionConfig.setAmountKey(5, stripCurrencyConverter);
        transactionConfig.setBalanceKey(5, stripCurrencyConverter); // TODO NO BALANCE???

        CSVRawDataParser csvRawDataParser = new CSVRawDataParser(';',10, 24, 27);

        return new Config<>(csvRawDataParser, statementConfig, transactionConfig);
    }

    @Override
    protected Config<?, ?> getConfig(DocumentType documentType) {
        return switch (documentType) {
            case CSV -> getCSVConfig();
            default -> null; // no compiler error, should never be executed
        };
    }
}
