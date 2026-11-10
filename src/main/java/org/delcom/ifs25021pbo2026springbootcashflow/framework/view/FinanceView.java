package org.delcom.ifs25021pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25021pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25021pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi Cashflow.
 * View hanya berinteraksi dengan use case dan presenter; semua aturan bisnis
 * (validasi, saldo, pengurutan) berada di layer dalam.
 */
@Route("")
@PageTitle("Cashflow - Catatan Keuangan")
public class FinanceView extends VerticalLayout {

    // Warna gelap agar kontras teks >= 4.5:1 (audit Accessibility)
    private static final String COLOR_INCOME = "#15803D";
    private static final String COLOR_EXPENSE = "#C2410C";
    private static final String COLOR_DANGER = "#B91C1C";

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span("- Data transaksi belum tersedia!");

    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span();
    private final Span expenseTotal = new Span();
    private final Span balanceLabel = new Span();

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureToolbarFields();

        add(new H1("Cashflow"), buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Atribut bahasa halaman (dinilai oleh audit Accessibility)
        attachEvent.getUI().getPage().executeJs("document.documentElement.lang = 'id';");
    }

    // ---------------------------------------------------------------- ringkasan

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createCard("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A", "#166534"),
                createCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626", "#991B1B"),
                createCard("Saldo", balanceLabel, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div createCard(String label, Span value, String bg, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "600")
                .set("color", valueColor);
        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("background", bg)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(financeUseCase.getAllTransactions().size()));
        incomeTotal.setText(presenter.formatMoney(financeUseCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatMoney(financeUseCase.getTotalExpense()));
        balanceLabel.setText(presenter.formatMoney(financeUseCase.getBalance()));
    }

    // --------------------------------------------------------------------- grid

    private void configureGrid() {
        grid.getElement().setAttribute("aria-label", "Daftar transaksi");
        grid.addColumn(Transaction::getId)
                .setHeader("ID").setKey("id")
                .setAutoWidth(true).setFlexGrow(0)
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getId));

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan").setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(t -> presenter.formatMoney(t.getAmount()))
                .setHeader("Jumlah").setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator());

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe").setKey("type")
                .setAutoWidth(true).setFlexGrow(0)
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator());

        grid.addColumn(t -> presenter.formatCreatedAt(t))
                .setHeader("Waktu").setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan").setKey("actions")
                .setAutoWidth(true).setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
    }

    private Span createTypeBadge(Transaction transaction) {
        boolean income = transaction.getType() == TransactionType.INCOME;
        Span badge = new Span(presenter.formatType(transaction.getType()));
        badge.getStyle()
                .set("padding", "0.15rem 0.6rem")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("color", income ? "#166534" : "#991B1B")
                .set("background", income ? "#DCFCE7" : "#FEE2E2");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button delete = new Button("Hapus", VaadinIcon.TRASH.create(), e -> openDeleteDialog(transaction));
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        delete.getStyle().set("color", COLOR_DANGER);
        return new HorizontalLayout(delete);
    }

    // ------------------------------------------------------------------ toolbar

    private void configureToolbarFields() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.getElement().setAttribute("aria-label", "Cari keterangan transaksi");

        sortSelect.setLabel("Urutkan");
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(presenter::formatSortOption);
        sortSelect.setPlaceholder("Pilih urutan");
        sortSelect.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                sortTransactions(e.getValue());
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), e -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refreshAll());

        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        incomeButton.getStyle().set("background", COLOR_INCOME).set("color", "#FFFFFF");

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        expenseButton.getStyle().set("background", COLOR_DANGER).set("color", "#FFFFFF");

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortSelect, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    // ------------------------------------------------------------------ dialogs

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000");
        amountField.setWidthFull();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(income ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        dialog.setWidth("450px");
        dialog.setMaxWidth("95vw");
        dialog.add(new VerticalLayout(descriptionField, amountField));

        Button save = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            BigDecimal amount;
            try {
                amount = parseAmount(amountField.getValue());
            } catch (NumberFormatException ex) {
                showError(presenter.showInvalidAmount());
                return;
            }
            try {
                Transaction created = income
                        ? financeUseCase.addIncome(descriptionField.getValue(), amount)
                        : financeUseCase.addExpense(descriptionField.getValue(), amount);
                showSuccess(income
                        ? presenter.showAddIncomeSuccess(created)
                        : presenter.showAddExpenseSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.getStyle().set("background", income ? COLOR_INCOME : COLOR_EXPENSE).set("color", "#FFFFFF");
        Button cancel = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());

        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Hapus Transaksi");
        dialog.add(new Paragraph("Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                + "\" (ID " + transaction.getId() + ")?"));

        Button confirm = new Button("Hapus", VaadinIcon.TRASH.create(), e -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirm.getStyle().set("background", COLOR_DANGER).set("color", "#FFFFFF");
        Button cancel = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());

        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    // ------------------------------------------------------------------ aksi

    /** Mengubah teks input menjadi angka; melempar NumberFormatException jika tidak valid. */
    private BigDecimal parseAmount(String text) {
        return new BigDecimal(text == null ? "" : text.trim());
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
            refreshAll();
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword == null || keyword.isBlank()) {
            refreshAll();
            return;
        }
        List<Transaction> results = financeUseCase.searchTransactions(keyword);
        emptyState.setText("- Transaksi tidak ditemukan!");
        refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void sortTransactions(SortOption option) {
        emptyState.setText("- Data transaksi belum tersedia!");
        refreshGrid(financeUseCase.sortTransactions(option),
                "Daftar Transaksi (" + presenter.formatSortOption(option) + ")");
    }

    private void refreshAll() {
        emptyState.setText("- Data transaksi belum tersedia!");
        searchField.clear();
        sortSelect.clear();
        refreshGrid(financeUseCase.getAllTransactions(), "Daftar Transaksi");
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        statusLabel.setText(transactions.isEmpty()
                ? title + " — " + emptyState.getText().replaceFirst("^- ", "")
                : title);
    }

    private void showSuccess(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}