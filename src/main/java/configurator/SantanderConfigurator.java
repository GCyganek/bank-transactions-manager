package configurator;

import configurator.config.Config;
import configurator.config.StatementConfig;
import configurator.config.TransactionConfig;
import importer.utils.Cell;
import importer.raw.CSVRawDataParser;
import importer.BankParser;
import importer.utils.converters.Converter;
import importer.utils.converters.DateConverter;
import importer.utils.converters.FloatToBigDecimalConverter;
import importer.utils.converters.IdentityConverter;
import model.BankType;
import model.DocumentType;
import repository.BankStatementsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SantanderConfigurator extends AbstractBankConfigurator {

    public SantanderConfigurator(BankStatementsRepository repository) {
        super(BankType.SANTANDER, repository);

        this.supportedDocumentTypes.addAll(List.of(DocumentType.CSV));
    }

    private Config<Cell, Integer> getCSVConfig() {
        StatementConfig<Cell> statementConfig = new StatementConfig<>();
        TransactionConfig<Integer> transactionConfig = new TransactionConfig<>();

        Converter<String> identity = new IdentityConverter();
        Converter<LocalDate> dateConverter = new DateConverter("dd-MM-yyyy");
        Converter<BigDecimal> FloatToBigDecimal = new FloatToBigDecimalConverter();

        statementConfig.setPeriodEndDateKey(new Cell(1, 1), new DateConverter("yyyy-MM-dd"));
        statementConfig.setPeriodStartDateKey(new Cell(1, 2), dateConverter);
        statementConfig.setAccountNumberKey(new Cell(1, 3), identity);
        statementConfig.setAccountOwnerKey(new Cell(1, 4), identity);
        statementConfig.setCurrencyKey(new Cell(1, 5), identity);
        statementConfig.setPaidInKey(new Cell(1, 6), FloatToBigDecimal);
        statementConfig.setPaidOutKey(new Cell(1, 7), FloatToBigDecimal);

        transactionConfig.setDateKey(1, dateConverter);
        transactionConfig.setDescriptionKey(3, identity);
        transactionConfig.setAmountKey(6, FloatToBigDecimal);
        transactionConfig.setBalanceKey(7, FloatToBigDecimal);

        CSVRawDataParser csvRawDataParser = new CSVRawDataParser(',', 1, 1, 2);

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
