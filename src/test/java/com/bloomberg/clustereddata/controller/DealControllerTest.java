package com.bloomberg.clustereddata.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bloomberg.clustereddata.dto.DealImportSummary;
import com.bloomberg.clustereddata.dto.DealRequest;
import com.bloomberg.clustereddata.dto.DealResponse;
import com.bloomberg.clustereddata.service.DealService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(controllers = DealController.class)
class DealControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private DealService dealService;

    @Test
    void createDeal_shouldReturnCreated() throws Exception {
        DealRequest request = DealRequest.builder()
                .dealUniqueId("FX-1")
                .fromCurrencyIso("USD")
                .toCurrencyIso("EUR")
                .dealTimestamp(Instant.parse("2024-11-25T10:00:00Z"))
                .dealAmount(new BigDecimal("10.5"))
                .build();

        DealResponse response = DealResponse.builder()
                .id(1L)
                .dealUniqueId("FX-1")
                .fromCurrencyIso("USD")
                .toCurrencyIso("EUR")
                .dealTimestamp(request.getDealTimestamp())
                .dealAmount(request.getDealAmount())
                .build();

        when(dealService.createDeal(any(DealRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dealUniqueId").value("FX-1"));
    }

    @Test
    void importDeals_shouldReturnSummary() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "deals.csv", "text/csv", "data".getBytes());
        DealImportSummary summary = DealImportSummary.builder()
                .totalRows(1)
                .successfulRows(1)
                .failedRows(0)
                .build();

        when(dealService.importDeals(any(MultipartFile.class))).thenReturn(summary);

        mockMvc.perform(multipart("/api/v1/deals/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulRows").value(1));

        verify(dealService).importDeals(any(MultipartFile.class));
    }
}

