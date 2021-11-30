package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Access(AccessType.PROPERTY)
@Table(name = BankStatement.TABLE_NAME)
public class BankStatement {

    public static final String TABLE_NAME = "bank_statement";

    private int id;

    private final StringProperty accountNumber = new SimpleStringProperty();

    private final ObjectProperty<LocalDate> periodStartDate = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDate> periodEndDate = new SimpleObjectProperty<>();

    private final ObjectProperty<BigDecimal> paidIn = new SimpleObjectProperty<>();

    private final ObjectProperty<BigDecimal> paidOut = new SimpleObjectProperty<>();

    private final StringProperty accountOwner = new SimpleStringProperty();

    private final StringProperty currency = new SimpleStringProperty();

    private Set<BankTransaction> bankTransactionSet = new HashSet<>();

    protected BankStatement() { }

    public BankStatement(final String accountNumber, final LocalDate periodStartDate, final LocalDate periodEndDate,
                         final BigDecimal paidIn, final BigDecimal paidOut, final String accountOwner, final String currency) {
        this.accountNumber.set(accountNumber);
        this.periodStartDate.set(periodStartDate);
        this.periodEndDate.set(periodEndDate);
        this.paidIn.set(paidIn);
        this.paidOut.set(paidOut);
        this.accountOwner.set(accountOwner);
        this.currency.set(currency);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Columns.ID)
    public int getId() {
        return id;
    }

    @Column(name = Columns.ACCOUNT_NUMBER, nullable = false, length = 64)
    public String getAccountNumber() {
        return accountNumber.get();
    }

    @Column(name = Columns.PERIOD_START_DATE, nullable = false)
    public LocalDate getPeriodStartDate() {
        return periodStartDate.get();
    }

    @Column(name = Columns.PERIOD_END_DATE, nullable = false)
    public LocalDate getPeriodEndDate() {
        return periodEndDate.get();
    }

    @Column(name = Columns.PAID_IN, nullable = false)
    public BigDecimal getPaidIn() {
        return paidIn.get();
    }

    @Column(name = Columns.PAID_OUT, nullable = false)
    public BigDecimal getPaidOut() {
        return paidOut.get();
    }

    @Column(name = Columns.ACCOUNT_OWNER, nullable = false, length = 128)
    public String getAccountOwner() {
        return accountOwner.get();
    }

    @Column(name = Columns.CURRENCY, nullable = false, length = 16)
    public String getCurrency() {
        return currency.get();
    }

    @OneToMany(mappedBy = "bankStatement", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Set<BankTransaction> getBankTransactionSet() {
        return bankTransactionSet;
    }

    public void setBankTransactionSet(Set<BankTransaction> bankTransactionSet) {
        this.bankTransactionSet = bankTransactionSet;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber.set(accountNumber);
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate.set(periodStartDate);
    }

    public void setPeriodEndDate(LocalDate periodEndDate) {
        this.periodEndDate.set(periodEndDate);
    }

    public void setPaidIn(BigDecimal paidIn) {
        this.paidIn.set(paidIn);
    }

    public void setPaidOut(BigDecimal paidOut) {
        this.paidOut.set(paidOut);
    }

    public void setAccountOwner(String accountOwner) {
        this.accountOwner.set(accountOwner);
    }

    public void setCurrency(String currency) {
        this.currency.set(currency);
    }

    public StringProperty accountNumberProperty() {
        return accountNumber;
    }

    public ObjectProperty<LocalDate> periodStartDateProperty() {
        return periodStartDate;
    }

    public ObjectProperty<LocalDate> periodEndDateProperty() {
        return periodEndDate;
    }

    public ObjectProperty<BigDecimal> paidInProperty() {
        return paidIn;
    }

    public ObjectProperty<BigDecimal> paidOutProperty() {
        return paidOut;
    }

    public StringProperty accountOwnerProperty() {
        return accountOwner;
    }

    public StringProperty currencyProperty() {
        return currency;
    }

    public static class Columns {
        public static final String ID = "id";

        public static final String ACCOUNT_NUMBER = "account_number";

        public static final String PERIOD_START_DATE = "period_start_date";

        public static final String PERIOD_END_DATE = "period_end_date";

        public static final String PAID_IN = "paid_in";

        public static final String PAID_OUT = "paid_out";

        public static final String ACCOUNT_OWNER = "account_owner";

        public static final String CURRENCY = "currency";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankStatement that = (BankStatement) o;
        return accountNumber.get().equals(that.accountNumber.get()) &&
                periodStartDate.get().equals(that.periodStartDate.get()) &&
                periodEndDate.get().equals(that.periodEndDate.get()) &&
                paidIn.get().equals(that.paidIn.get()) && paidOut.get().equals(that.paidOut.get()) &&
                accountOwner.get().equals(that.accountOwner.get()) && currency.get().equals(that.currency.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber.get(), periodStartDate.get(), periodEndDate.get(), paidIn.get(),
                paidOut.get(), accountOwner.get(), currency.get());
    }

    @Override
    public String toString() {
        return String.format("Bank Statement: [(%s), (%s), (%s), (%s), (%s), (%s)]",
                accountNumber, periodStartDate, periodEndDate,
                paidIn, paidOut, accountOwner);
    }
}
