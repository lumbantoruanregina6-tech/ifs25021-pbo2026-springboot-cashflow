package org.delcom.ifs25021pbo2026springbootcashflow.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity inti: satu transaksi keuangan (pemasukan atau pengeluaran).
 * Bebas dari urusan tampilan maupun penyimpanan.
 */
public class Transaction {
    /** ID unik, tidak boleh diubah setelah dibuat. */
    private final int id;

    /** Keterangan transaksi. */
    private String description;

    /** Jumlah uang (selalu positif). */
    private BigDecimal amount;

    /** Jenis transaksi. */
    private final TransactionType type;

    /** Waktu transaksi dicatat. */
    private final LocalDateTime createdAt;

    public Transaction(int id, String description, BigDecimal amount, TransactionType type) {
        this(id, description, amount, type, LocalDateTime.now());
    }

    public Transaction(int id, String description, BigDecimal amount, TransactionType type,
                       LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
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

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Mengubah keterangan transaksi. */
    public void changeDescription(String description) {
        this.description = description;
    }

    /** Mengubah jumlah transaksi. */
    public void changeAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /** Pengaruh transaksi ke saldo: positif untuk pemasukan, negatif untuk pengeluaran. */
    public BigDecimal signedAmount() {
        return type == TransactionType.INCOME ? amount : amount.negate();
    }
}
