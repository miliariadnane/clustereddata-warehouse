package com.bloomberg.clustereddata.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;

@Builder
public record DealResponse(Long id, String dealUniqueId, String fromCurrencyIso, String toCurrencyIso,
                           Instant dealTimestamp, BigDecimal dealAmount) {
}

