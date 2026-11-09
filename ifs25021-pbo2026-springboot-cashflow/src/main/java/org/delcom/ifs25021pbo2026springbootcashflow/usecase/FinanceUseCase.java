package org.delcom.ifs25021pbo2026springbootcashflow.usecase;

import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Use case pencatatan keuangan. Tanpa I/O UI: hanya memproses data dan
 * melempar checked exception domain bila operasi tidak valid.
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambah pemasukan setelah validasi. */
    public Transaction addIncome(String description, BigDecimal amount) throws ValidationException {
        validate(description, amount);
        return transactionRepository.saveIncome(description.trim(), amount);
    }

    /** Menambah pengeluaran setelah validasi. */
    public Transaction addExpense(String description, BigDecimal amount) throws ValidationException {
        validate(description, amount);
        return transactionRepository.saveExpense(description.trim(), amount);
    }

    /** Menghapus transaksi; melempar exception jika ID tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!transactionRepository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi berdasarkan keterangan (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String lower = keyword == null ? "" : keyword.trim().toLowerCase();
        return transactionRepository.findBy(t -> t.getDescription().toLowerCase().contains(lower));
    }

    /** Mengurutkan transaksi sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(SortOption option) {
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public BigDecimal getTotalIncome() {
        return totalOf(TransactionType.INCOME);
    }

    /** Total seluruh pengeluaran. */
    public BigDecimal getTotalExpense() {
        return totalOf(TransactionType.EXPENSE);
    }

    /** Saldo = total pemasukan - total pengeluaran. */
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    private BigDecimal totalOf(TransactionType type) {
        return transactionRepository.findBy(t -> t.getType() == type).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validate(String description, BigDecimal amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
    }
}
