package configurator;

import model.BankType;
import repository.BankStatementsRepository;

public class BankConfiguratorFactory {
    private final BankStatementsRepository repository;

    // TODO not sure about this dependency either (factory -> configurator -> builder)
    public BankConfiguratorFactory(BankStatementsRepository repository) {
        this.repository = repository;
    }

    public BankConfigurator createBankConfigurator(BankType bankType) {
        return switch (bankType) {
            case SANTANDER -> new SantanderConfigurator(repository);
            case MBANK -> new MBankConfigurator(repository);
        };
    }
}
