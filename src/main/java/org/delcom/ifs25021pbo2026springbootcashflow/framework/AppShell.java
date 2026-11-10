package org.delcom.ifs25021pbo2026springbootcashflow.framework;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;

/**
 * Konfigurasi level aplikasi Vaadin. Anotasi level halaman seperti {@link Meta}
 * wajib ditempatkan di sini, bukan di kelas @Route.
 */
@Meta(name = "description",
        content = "Aplikasi Cashflow untuk mencatat pemasukan dan pengeluaran serta memantau saldo keuangan pribadi.")
public class AppShell implements AppShellConfigurator {
}
