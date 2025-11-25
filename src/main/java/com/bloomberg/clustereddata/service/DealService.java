package com.bloomberg.clustereddata.service;

import com.bloomberg.clustereddata.domain.Deal;
import com.bloomberg.clustereddata.dto.DealImportFailure;
import com.bloomberg.clustereddata.dto.DealImportSummary;
import com.bloomberg.clustereddata.dto.DealRequest;
import com.bloomberg.clustereddata.dto.DealResponse;
import com.bloomberg.clustereddata.exception.DealAlreadyExistsException;
import com.bloomberg.clustereddata.util.CsvDealParser;
import com.bloomberg.clustereddata.util.DealCsvRow;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {

    private final DealPersistenceService dealPersistenceService;
    private final CsvDealParser csvDealParser;
    private final Validator validator;

    public DealResponse createDeal(DealRequest request) {
        Deal savedDeal = dealPersistenceService.save(toEntity(request));
        log.info("Persisted deal with uniqueId={}", savedDeal.getDealUniqueId());
        return toResponse(savedDeal);
    }

    public DealImportSummary importDeals(MultipartFile csvFile) {
        List<DealCsvRow> rows = csvDealParser.parse(csvFile);
        if (rows.isEmpty()) {
            return DealImportSummary.builder()
                    .totalRows(0)
                    .successfulRows(0)
                    .failedRows(0)
                    .build();
        }

        DealImportSummary.DealImportSummaryBuilder summaryBuilder =
                DealImportSummary.builder().totalRows(rows.size());

        int successful = 0;
        for (DealCsvRow row : rows) {
            DealImportFailure failure = validateAndPersistRow(row);
            if (failure == null) {
                successful++;
            } else {
                summaryBuilder.failure(failure);
            }
        }

        return summaryBuilder
                .successfulRows(successful)
                .failedRows(rows.size() - successful)
                .build();
    }

    private Deal toEntity(DealRequest request) {
        return Deal.builder()
                .dealUniqueId(request.getDealUniqueId())
                .fromCurrencyIso(request.getFromCurrencyIso())
                .toCurrencyIso(request.getToCurrencyIso())
                .dealTimestamp(request.getDealTimestamp())
                .dealAmount(request.getDealAmount())
                .build();
    }

    private DealResponse toResponse(Deal deal) {
        return DealResponse.builder()
                .id(deal.getId())
                .dealUniqueId(deal.getDealUniqueId())
                .fromCurrencyIso(deal.getFromCurrencyIso())
                .toCurrencyIso(deal.getToCurrencyIso())
                .dealTimestamp(deal.getDealTimestamp())
                .dealAmount(deal.getDealAmount())
                .build();
    }

    private String formatViolations(Set<ConstraintViolation<DealRequest>> violations) {
        return violations.stream()
                .map(violation -> "%s %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                .collect(Collectors.joining("; "));
    }

    private DealImportFailure validateAndPersistRow(DealCsvRow row) {
        if (row.hasError()) {
            return buildFailure(row, "CSV parsing error: " + row.errorMessage());
        }

        Set<ConstraintViolation<DealRequest>> violations = validator.validate(row.dealRequest());
        if (!violations.isEmpty()) {
            return buildFailure(row, formatViolations(violations));
        }

        try {
            dealPersistenceService.save(toEntity(row.dealRequest()));
            return null;
        } catch (DealAlreadyExistsException duplicate) {
            return buildFailure(row, duplicate.getMessage());
        } catch (Exception exception) {
            log.error("Failed to import row {}", row.rowNumber(), exception);
            return buildFailure(row, "Unexpected error: " + exception.getMessage());
        }
    }

    private DealImportFailure buildFailure(DealCsvRow row, String reason) {
        return DealImportFailure.builder()
                .rowNumber(row.rowNumber())
                .reason(reason)
                .build();
    }
}

