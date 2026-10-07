package com.papaya.assessment.payroll.controller;

import com.papaya.assessment.payroll.client.ExternalPayrollClient;
import com.papaya.assessment.payroll.dto.ExternalPayrollRecordDto;
import com.papaya.assessment.payroll.entity.Payroll;
import com.papaya.assessment.payroll.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "payroll-sync", roles = "SYNC_ADMIN")
class PayrollControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PayrollRepository payrollRepository;

    @MockBean
    private ExternalPayrollClient externalPayrollClient;

    @BeforeEach
    void cleanDb() {
        payrollRepository.deleteAll();
    }

    @Test
    void syncAndListPayroll() throws Exception {
        when(externalPayrollClient.fetchPayrollRecords()).thenReturn(List.of(sample()));

        mockMvc.perform(post("/api/payroll/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persisted").value(1));

        mockMvc.perform(get("/api/payroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employeeId").value("EMP001"));
    }

    @Test
    void filterByPayPeriod() throws Exception {
        payrollRepository.save(entity("EMP001", "2026-09"));
        payrollRepository.save(entity("EMP002", "2026-08"));

        mockMvc.perform(get("/api/payroll").param("payPeriod", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].payPeriod").value("2026-09"));
    }

    @Test
    void getEmployeePayroll() throws Exception {
        payrollRepository.save(entity("EMP001", "2026-09"));

        mockMvc.perform(get("/api/payroll/EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value("EMP001"));
    }

    @Test
    void employeeNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/payroll/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PAYROLL_NOT_FOUND"));
    }

    @Test
    void duplicateSyncIsIdempotent() throws Exception {
        when(externalPayrollClient.fetchPayrollRecords()).thenReturn(List.of(sample()));

        mockMvc.perform(post("/api/payroll/sync")).andExpect(status().isOk());
        mockMvc.perform(post("/api/payroll/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skippedDuplicate").value(1));

        mockMvc.perform(get("/api/payroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    private static ExternalPayrollRecordDto sample() {
        return new ExternalPayrollRecordDto(
                "EMP001", "John Doe", "2026-09",
                new BigDecimal("75000.00"), new BigDecimal("12000.00"),
                new BigDecimal("63000.00"), "USD");
    }

    private static Payroll entity(String employeeId, String payPeriod) {
        Payroll payroll = new Payroll();
        payroll.setEmployeeId(employeeId);
        payroll.setEmployeeName("Name");
        payroll.setPayPeriod(payPeriod);
        payroll.setGrossSalary(new BigDecimal("100"));
        payroll.setTax(new BigDecimal("10"));
        payroll.setNetSalary(new BigDecimal("90"));
        payroll.setCurrency("USD");
        return payroll;
    }
}
