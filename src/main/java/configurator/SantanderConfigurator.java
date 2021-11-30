package configurator;

import configurator.config.Config;
import configurator.config.StatementConfig;
import configurator.config.TransactionConfig;
import model.BankStatementBuilder;
import importer.raw.CSVRawDataParser;
import importer.BankParser;
import importer.utils.Converter;
import model.DocumentType;
import repository.BankStatementsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SantanderConfigurator implements BankConfigurator {

    private BankStatementsRepository repository;

    // TODO not sure about this dependency (here and in builder)
    public SantanderConfigurator(BankStatementsRepository repository)  {
        this.repository = repository;
    }

    private Config<Integer> configureCSV() {
        StatementConfig<Integer> statementConfig = new StatementConfig<>();
        TransactionConfig<Integer> transactionConfig = new TransactionConfig<>();

        Converter<String> identity = x -> x;
        Converter<LocalDate> dateConverter = x -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(x, formatter);
        };

        Converter<BigDecimal> FloatToBigDecimal = x -> new BigDecimal(x.replace(',', '.'));


        statementConfig.setPeriodEndDateKey(0, LocalDate::parse);
        statementConfig.setPeriodStartDateKey(1, dateConverter);
        statementConfig.setAccountNumberKey(2, identity);
        statementConfig.setAccountOwnerKey(3, identity);
        statementConfig.setPaidInKey(5, FloatToBigDecimal);
        statementConfig.setPaidOutKey(6, FloatToBigDecimal);

        transactionConfig.setDateKey(0, dateConverter);
        transactionConfig.setDescriptionKey(2, identity);
        transactionConfig.setAmountKey(5, FloatToBigDecimal);
        transactionConfig.setBalanceKey(6, FloatToBigDecimal);

        return new Config<>(transactionConfig, statementConfig);
    }

    private BankParser<Integer> createCSVParser(Config<Integer> config) {
        CSVRawDataParser csvRawDataParser = new CSVRawDataParser();
        BankStatementBuilder<Integer> bankStatementBuilder =
                        new BankStatementBuilder<>(repository,
                            config.getStatementConfig(),
                            config.getTransactionConfig());

        return new BankParser<>(csvRawDataParser, config, bankStatementBuilder);
    }

    @Override
    public BankParser<?> configureParser(DocumentType documentType) {
        return switch (documentType) {
            case CSV -> createCSVParser(configureCSV());
        };
    }
}
