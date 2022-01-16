package watcher.builder;

import model.util.BankType;

public interface BankTypeStep {
    DescriptionStep withBankType(BankType bankType);
}
