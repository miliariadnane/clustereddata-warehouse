package com.bloomberg.clustereddata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bloomberg.clustereddata.domain.Deal;
import com.bloomberg.clustereddata.dto.DealImportSummary;
import com.bloomberg.clustereddata.dto.DealRequest;
import com.bloomberg.clustereddata.dto.DealResponse;
import com.bloomberg.clustereddata.exception.DealAlreadyExistsException;
import com.bloomberg.clustereddata.util.CsvDealParser;
import com.bloomberg.clustereddata.util.DealCsvRow;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealPersistenceService dealPersistenceService;

    @Mock
    private CsvDealParser csvDealParser;

    @Mock
    private Validator validator;

    private DealService dealService;

    @BeforeEach
    void setUp() {
        dealService = new DealService(dealPersistenceService, csvDealParser, validator);
    }

    @Test
    void createDeal_shouldPersistAndReturnResponse() {
        DealRequest request = buildRequest("FX-1");
        Deal persistedDeal = Deal.builder()
                .id(42L)
                .dealUniqueId(request.getDealUniqueId())
                .fromCurrencyIso(request.getFromCurrencyIso())
                .toCurrencyIso(request.getToCurrencyIso())
                .dealAmount(request.getDealAmount())
                .dealTimestamp(request.getDealTimestamp())
                .build();

        when(dealPersistenceService.save(any(Deal.class))).thenReturn(persistedDeal);

        DealResponse response = dealService.createDeal(request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.dealUniqueId()).isEqualTo("FX-1");
    }

    @Test
    void importDeals_shouldReturnSummaryWithFailures() {
        MockMultipartFile csv = new MockMultipartFile("file", "deals.csv", "text/csv", new byte[] {});
        DealRequest request1 = buildRequest("FX-1");
        DealRequest request2 = buildRequest("FX-duplicate");
        DealCsvRow row1 = new DealCsvRow(1, request1, null);
        DealCsvRow row2 = new DealCsvRow(2, request2, null);
        DealCsvRow row3 = new DealCsvRow(3, null, "Broken row");

        when(csvDealParser.parse(csv)).thenReturn(List.of(row1, row2, row3));
        when(validator.validate(request1)).thenReturn(Collections.emptySet());
        when(validator.validate(request2)).thenReturn(Collections.emptySet());
        when(dealPersistenceService.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(new DealAlreadyExistsException("FX-duplicate"));

        DealImportSummary summary = dealService.importDeals(csv);

        assertThat(summary.totalRows()).isEqualTo(3);
        assertThat(summary.successfulRows()).isEqualTo(1);
        assertThat(summary.failedRows()).isEqualTo(2);
        assertThat(summary.failures()).hasSize(2);
    }

    private DealRequest buildRequest(String dealId) {
        return DealRequest.builder()
                .dealUniqueId(dealId)
                .fromCurrencyIso("USD")
                .toCurrencyIso("EUR")
                .dealTimestamp(Instant.parse("2024-11-25T12:00:00Z"))
                .dealAmount(new BigDecimal("100.50"))
                .build();
    }
}

