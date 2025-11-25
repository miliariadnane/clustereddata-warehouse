package com.bloomberg.clustereddata.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bloomberg.clustereddata.dto.DealImportSummary;
import com.bloomberg.clustereddata.dto.DealRequest;
import com.bloomberg.clustereddata.dto.DealResponse;
import com.bloomberg.clustereddata.service.DealService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class DealServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Autowired private DealService dealService;

    @Autowired private DealRepository dealRepository;

    @AfterEach
    void tearDown() {
        dealRepository.deleteAll();
    }

    @Test
    void createDeal_shouldPersistIntoDatabase() {
        DealRequest request = DealRequest.builder()
                .dealUniqueId("FX-IT-1")
                .fromCurrencyIso("USD")
                .toCurrencyIso("CAD")
                .dealTimestamp(Instant.parse("2024-11-25T14:00:00Z"))
                .dealAmount(new BigDecimal("5000.00"))
                .build();

        DealResponse response = dealService.createDeal(request);

        assertThat(response.id()).isNotNull();
        assertThat(dealRepository.existsByDealUniqueId("FX-IT-1")).isTrue();
    }

    @Test
    void importDeals_shouldImportMultipleRows() {
        String csv =
                """
                deal_unique_id,from_currency_iso,to_currency_iso,deal_timestamp,deal_amount
                FX-IT-CSV-1,USD,EUR,2024-11-25T10:15:30Z,1000.00
                FX-IT-CSV-2,EUR,JPY,2024-11-25T11:15:30Z,2000.00
                """;
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "deals.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        DealImportSummary summary = dealService.importDeals(multipartFile);

        assertThat(summary.successfulRows()).isEqualTo(2);
        assertThat(dealRepository.count()).isEqualTo(2);
    }
}

