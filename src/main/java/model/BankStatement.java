package model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = BankStatement.TABLE_NAME)
public class BankStatement {

    public static final String TABLE_NAME = "bank_statement";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Columns.ID)
    private int id;

    @Column(name = Columns.ACCOUNT_NUMBER, nullable = false, length = 64)
    private String accountNumber;

    @Column(name = Columns.PERIOD_START_DATE, nullable = false)
    private LocalDate periodStartDate;

    @Column(name = Columns.PERIOD_END_DATE, nullable = false)
    private LocalDate periodEndDate;

    @Column(name = Columns.PAID_IN, nullable = false)
    private BigDecimal paidIn;

    @Column(name = Columns.PAID_OUT, nullable = false)
    private BigDecimal paidOut;

    @Column(name = Columns.ACCOUNT_OWNER, nullable = false, length = 128)
    private String accountOwner;

    @OneToMany(mappedBy = "bankStatement", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final Set<BankTransaction> bankTransactionSet = new HashSet<>();

    protected BankStatement() { }

    public BankStatement(final String accountNumber, final LocalDate periodStartDate, final LocalDate periodEndDate,
                         final BigDecimal paidIn, final BigDecimal paidOut, final String accountOwner) {
        this.accountNumber = accountNumber;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.paidIn = paidIn;
        this.paidOut = paidOut;
        this.accountOwner = accountOwner;
    }

    public int getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public LocalDate getPeriodEndDate() {
        return periodEndDate;
    }

    public BigDecimal getPaidIn() {
        return paidIn;
    }

    public BigDecimal getPaidOut() {
        return paidOut;
    }

    public String getAccountOwner() {
        return accountOwner;
    }

    public Set<BankTransaction> getBankTransactionSet() {
        return bankTransactionSet;
    }

    public static class Columns {
        public static final String ID = "id";

        public static final String ACCOUNT_NUMBER = "account_number";

        public static final String PERIOD_START_DATE = "period_start_date";

        public static final String PERIOD_END_DATE = "period_end_date";

        public static final String PAID_IN = "paid_in";

        public static final String PAID_OUT = "paid_out";

        public static final String ACCOUNT_OWNER = "account_owner";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankStatement that = (BankStatement) o;
        return id == that.id && accountNumber.equals(that.accountNumber) && periodStartDate.equals(that.periodStartDate)
                && periodEndDate.equals(that.periodEndDate) && paidIn.equals(that.paidIn) &&
                paidOut.equals(that.paidOut) && accountOwner.equals(that.accountOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, periodStartDate, periodEndDate, paidIn, paidOut, accountOwner);
    }
}
