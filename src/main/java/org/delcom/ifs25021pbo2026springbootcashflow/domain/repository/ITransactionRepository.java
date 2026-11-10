package org.delcom.ifs25021pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;

/**
 * Port spesifik Transaction. Menambahkan pembuatan transaksi baru
 * dengan penomoran ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan pemasukan baru; mengembalikan transaksi lengkap dengan ID. */
    Transaction saveIncome(String description, BigDecimal amount);

    /** Menyimpan pengeluaran baru; mengembalikan transaksi lengkap dengan ID. */
    Transaction saveExpense(String description, BigDecimal amount);
}
