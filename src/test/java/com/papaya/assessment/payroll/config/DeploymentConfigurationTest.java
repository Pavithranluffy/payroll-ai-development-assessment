package com.papaya.assessment.payroll.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentConfigurationTest {

    @Test
    void composeFileDoesNotContainHardcodedSecrets() throws IOException {
        Path composeFile = Path.of("docker-compose.yml");
        String compose = Files.readString(composeFile);

        assertThat(compose)
                .doesNotContain("POSTGRES_PASSWORD: payroll")
                .doesNotContain("POSTGRES_USER: payroll")
                .doesNotContain("POSTGRES_DB: payroll");
        assertThat(compose)
                .contains("POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD}")
                .contains("POSTGRES_USER: ${POSTGRES_USER:?Set POSTGRES_USER}")
                .contains("POSTGRES_DB: ${POSTGRES_DB:?Set POSTGRES_DB}");
    }
}
