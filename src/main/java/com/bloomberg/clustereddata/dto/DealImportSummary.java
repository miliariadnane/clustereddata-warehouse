package com.bloomberg.clustereddata.dto;

import java.util.List;
import lombok.Builder;
import lombok.Singular;

@Builder
public record DealImportSummary(int totalRows, int successfulRows, int failedRows,
                                @Singular List<DealImportFailure> failures) {
}

