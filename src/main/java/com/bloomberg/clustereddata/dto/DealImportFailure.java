package com.bloomberg.clustereddata.dto;

import lombok.Builder;

@Builder
public record DealImportFailure(int rowNumber, String reason) {
}

