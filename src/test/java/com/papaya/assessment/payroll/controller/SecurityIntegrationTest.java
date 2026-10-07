package com.papaya.assessment.payroll.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.papaya.assessment.payroll.client.ExternalPayrollClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalPayrollClient externalPayrollClient;

    @Autowired
    private Environment environment;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/payroll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void h2ConsoleIsDisabledByDefault() {
        assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class)).isFalse();
    }

    @Test
    @WithMockUser(username = "user")
    void syncEndpointRejectsAuthenticatedUserWithoutAdminRole() throws Exception {
        mockMvc.perform(post("/api/payroll/sync"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "SYNC_ADMIN")
    void syncEndpointAllowsConfiguredAdminRole() throws Exception {
        when(externalPayrollClient.fetchPayrollRecords()).thenReturn(List.of());

        mockMvc.perform(post("/api/payroll/sync"))
                .andExpect(status().isOk());
    }
}
