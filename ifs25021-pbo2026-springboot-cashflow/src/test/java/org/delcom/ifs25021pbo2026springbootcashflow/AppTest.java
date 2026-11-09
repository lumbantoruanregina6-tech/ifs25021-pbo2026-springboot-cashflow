package org.delcom.ifs25021pbo2026springbootcashflow;

import org.delcom.ifs25021pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25021pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25021pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Smoke test: wiring bean Clean Architecture dan delegasi App.main. */
@SpringBootTest
class AppTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsAndBeansAreWired() {
        assertNotNull(context.getBean(ITransactionRepository.class));
        assertNotNull(context.getBean(FinanceUseCase.class));
        assertNotNull(context.getBean(FinancePresenter.class));
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            String[] args = {};
            App.main(args);
            mocked.verify(() -> SpringApplication.run(App.class, args));
        }
    }
}
