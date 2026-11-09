package org.delcom.ifs25021pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi. Setiap opsi membawa comparator-nya sendiri
 * sehingga logika pengurutan terpusat di domain.
 */
public enum SortOption {
    /** Jumlah terkecil lebih dulu. */
    AMOUNT_ASC(Comparator.comparing(Transaction::getAmount)),

    /** Jumlah terbesar lebih dulu. */
    AMOUNT_DESC(Comparator.comparing(Transaction::getAmount).reversed()),

    /** Pemasukan lebih dulu (INCOME berurutan sebelum EXPENSE). */
    INCOME_FIRST(Comparator.comparing(Transaction::getType)),

    /** Pengeluaran lebih dulu. */
    EXPENSE_FIRST(Comparator.comparing(Transaction::getType).reversed());

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    /** Comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
