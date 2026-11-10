package org.delcom.ifs25021pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter: memformat data dari use case menjadi teks siap tampil
 * (uang, tipe, baris transaksi, saldo, dan pesan notifikasi).
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Format uang: 1000000 -> "Rp 1,000,000.00". Nilai negatif: "-Rp 500.00". */
    public String formatMoney(BigDecimal amount) {
        String text = String.format(Locale.US, "%,.2f", amount.abs());
        return (amount.signum() < 0 ? "-Rp " : "Rp ") + text;
    }

    /** Label tipe transaksi. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Label opsi pengurutan. */
    public String formatSortOption(SortOption option) {
        return switch (option) {
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
        };
    }

    /** Format waktu transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Satu baris transaksi: [id] keterangan | Rp x | [Tipe]. */
    public String format(Transaction t) {
        return String.format("[%d] %s | %s | [%s]",
                t.getId(), t.getDescription(), formatMoney(t.getAmount()), formatType(t.getType()));
    }

    /** Teks saldo: "Saldo: Rp x". */
    public String formatBalance(BigDecimal balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Daftar transaksi multi-baris; pesan kosong jika tidak ada data. */
    public String formatList(List<Transaction> list, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (list.isEmpty()) {
            return builder.append('\n').append(emptyMessage).toString();
        }
        return builder.append('\n')
                .append(list.stream().map(this::format).collect(Collectors.joining("\n")))
                .toString();
    }

    public String showTransactions(List<Transaction> list) {
        return formatList(list, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    public String showSearchResults(List<Transaction> list, String keyword) {
        return formatList(list, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    public String showSortedTransactions(List<Transaction> list) {
        return formatList(list, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    public String showAddIncomeSuccess(Transaction t) {
        return "Berhasil menambah pemasukan: " + format(t);
    }

    public String showAddExpenseSuccess(Transaction t) {
        return "Berhasil menambah pengeluaran: " + format(t);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showInvalidAmount() {
        return "[!] Jumlah harus berupa angka yang valid!";
    }

    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    public String showInvalidChoice() {
        return "[!] Pilihan tidak dimengerti.";
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
