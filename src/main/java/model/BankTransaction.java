package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Access(AccessType.PROPERTY)
@Table(name = BankTransaction.TABLE_NAME)
public class BankTransaction {

    public static final String TABLE_NAME = "bank_transaction";

    private int id;

    private final StringProperty description = new SimpleStringProperty();

    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    private final ObjectProperty<BigDecimal> balance = new SimpleObjectProperty<>();

    private BankStatement bankStatement;

    protected BankTransaction() {}

    public BankTransaction(final String description, final BigDecimal amount, final LocalDate date, final BigDecimal balance) {
        this.description.set(description);
        this.amount.set(amount);
        this.date.set(date);
        this.balance.set(balance);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Columns.ID)
    public int getId() {
        return id;
    }

    @Column(name = Columns.DESCRIPTION, nullable = false, length = 512)
    public String getDescription() {
        return description.get();
    }

    @Column(name = Columns.AMOUNT, nullable = false)
    public BigDecimal getAmount() {
        return amount.get();
    }

    @Column(name = Columns.DATE, nullable = false)
    public LocalDate getDate() {
        return date.get();
    }

    @Column(name = Columns.BALANCE, nullable = false)
    public BigDecimal getBalance() {
        return balance.get();
    }

    @ManyToOne
    @JoinColumn(name = Columns.STATEMENT_ID)
    public BankStatement getBankStatement() {
        return bankStatement;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<BigDecimal> amountProperty() {
        return amount;
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public ObjectProperty<BigDecimal> balanceProperty() {
        return balance;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setAmount(BigDecimal amount) {
        this.amount.set(amount);
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public void setBalance(BigDecimal balance) {
        this.balance.set(balance);
    }

    public void setBankStatement(BankStatement bankStatement) {
        this.bankStatement = bankStatement;
    }

    public static class Columns {

        public static final String ID = "id";

        public static final String DESCRIPTION = "description";

        public static final String AMOUNT = "amount";

        public static final String DATE = "date";

        public static final String BALANCE = "balance";

        public static final String STATEMENT_ID = "statement_id";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankTransaction that = (BankTransaction) o;
        return description.get().equals(that.description.get()) && amount.get().equals(that.amount.get()) &&
                date.get().equals(that.date.get()) && balance.get().equals(that.balance.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(description.get(), amount.get(), date.get(), balance.get());
    }

    @Override
    public String toString() {
        return String.format("Transaction: [(%s), (%s), (%s), (%s)]",
                description.get(), amount.get(), date.get(), balance.get());
    }
}
