package com.bloomberg.clustereddata.dto;

import com.bloomberg.clustereddata.validation.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DealRequest {

    @NotBlank
    @Size(max = 64)
    String dealUniqueId;

    @NotBlank
    @CurrencyCode
    String fromCurrencyIso;

    @NotBlank
    @CurrencyCode
    String toCurrencyIso;

    @NotNull
    Instant dealTimestamp;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    BigDecimal dealAmount;
}

