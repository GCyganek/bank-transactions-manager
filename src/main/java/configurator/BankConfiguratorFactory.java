package configurator;

import model.BankType;
import repository.BankStatementsRepository;

public class BankConfiguratorFactory {
    public BankConfigurator createBankConfigurator(BankType bankType) {
        return switch (bankType) {
            case SANTANDER -> new SantanderConfigurator();
            case MBANK -> new MBankConfigurator();
        };
    }
}
