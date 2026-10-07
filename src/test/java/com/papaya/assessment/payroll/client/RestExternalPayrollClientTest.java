package com.papaya.assessment.payroll.client;

import com.papaya.assessment.payroll.config.PayrollApiProperties;
import com.papaya.assessment.payroll.exception.ExternalApiClientException;
import com.papaya.assessment.payroll.exception.ExternalApiException;
import com.papaya.assessment.payroll.exception.ExternalApiTransientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

class RestExternalPayrollClientTest {

        private MockRestServiceServer server;
        private RestExternalPayrollClient client;

        @BeforeEach
        void setUp() {
                PayrollApiProperties properties = new PayrollApiProperties(
                                "http://localhost:9999", "/api/v1/payroll-records", 1000, 3000, "");
                RestClient.Builder builder = RestClient.builder();
                server = MockRestServiceServer.bindTo(builder).build();
                client = new RestExternalPayrollClient(builder, properties);
        }

        @AfterEach
        void verify() {
                server.verify();
        }

        @Test
        void fetchesRecordsSuccessfully() {
                String json = """
                                [{"employeeId":"EMP001","employeeName":"John","payPeriod":"2026-09",
                                "grossSalary":75000.00,"tax":12000.00,"netSalary":63000.00,"currency":"USD"}]
                                """;
                server.expect(requestTo("http://localhost:9999/api/v1/payroll-records"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

                var records = client.fetchPayrollRecords();

                assertThat(records).hasSize(1);
                assertThat(records.get(0).employeeId()).isEqualTo("EMP001");
        }

        @Test
        void mapsHttp400ToClientException() {
                server.expect(requestTo("http://localhost:9999/api/v1/payroll-records"))
                                .andRespond(withBadRequest());

                assertThatThrownBy(() -> client.fetchPayrollRecords())
                                .isInstanceOf(ExternalApiClientException.class);
        }

        @Test
        void mapsHttp429ToTransientException() {
                server.expect(requestTo("http://localhost:9999/api/v1/payroll-records"))
                                .andRespond(withTooManyRequests());

                assertThatThrownBy(() -> client.fetchPayrollRecords())
                                .isInstanceOf(ExternalApiTransientException.class);
        }

        @Test
        void mapsHttp503ToTransientException() {
                server.expect(requestTo("http://localhost:9999/api/v1/payroll-records"))
                                .andRespond(withServiceUnavailable());

                assertThatThrownBy(() -> client.fetchPayrollRecords())
                                .isInstanceOf(ExternalApiTransientException.class);
        }

        @Test
        void mapsMalformedJsonToExternalApiException() {
                server.expect(requestTo("http://localhost:9999/api/v1/payroll-records"))
                                .andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));

                assertThatThrownBy(() -> client.fetchPayrollRecords())
                                .isInstanceOf(ExternalApiException.class);
        }
}
