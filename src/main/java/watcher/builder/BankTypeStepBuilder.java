package watcher.builder;

import model.util.BankType;

public interface BankTypeStepBuilder {
    DescriptionStepBuilder withBankType(BankType bankType);
}
