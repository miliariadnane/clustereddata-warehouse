package com.bloomberg.clustereddata.util;

import com.bloomberg.clustereddata.dto.DealRequest;

public record DealCsvRow(int rowNumber, DealRequest dealRequest, String errorMessage) {

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}

