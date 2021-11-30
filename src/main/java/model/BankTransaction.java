package model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = BankTransaction.TABLE_NAME)
public class BankTransaction {

    public static final String TABLE_NAME = "bank_transaction";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Columns.ID)
    private int id;

    @Column(name = Columns.DESCRIPTION, nullable = false, length = 512)
    private String description;

    @Column(name = Columns.AMOUNT, nullable = false)
    private BigDecimal amount;

    @Column(name = Columns.DATE, nullable = false)
    private LocalDate date;

    @Column(name = Columns.BALANCE, nullable = false)
    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = Columns.STATEMENT_ID)
    private BankStatement bankStatement;

    protected BankTransaction() {}

    public BankTransaction(final String description, final BigDecimal amount, final LocalDate date, final BigDecimal balance) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BankStatement getBankStatement() {
        return bankStatement;
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
        return id == that.id && description.equals(that.description)
                && amount.equals(that.amount) && date.equals(that.date) && balance.equals(that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, amount, date, balance);
    }

    @Override
    public String toString() {
        return String.format("Transaction: [(%s), (%s), (%s), (%s)]",
                description, amount, date, balance);
    }
}
