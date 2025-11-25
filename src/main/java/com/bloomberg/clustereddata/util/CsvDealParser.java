package com.bloomberg.clustereddata.util;

import com.bloomberg.clustereddata.dto.DealRequest;
import com.bloomberg.clustereddata.exception.InvalidCsvException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvDealParser {

    private static final String DEAL_UNIQUE_ID = "deal_unique_id";
    private static final String FROM_CURRENCY_ISO = "from_currency_iso";
    private static final String TO_CURRENCY_ISO = "to_currency_iso";
    private static final String DEAL_TIMESTAMP = "deal_timestamp";
    private static final String DEAL_AMOUNT = "deal_amount";

    public List<DealCsvRow> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCsvException("CSV file is empty.");
        }

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser =
                        CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim()
                                .parse(reader)) {

            validateHeaders(parser);
            List<DealCsvRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                int rowNumber = Math.toIntExact(record.getRecordNumber());
                try {
                    DealRequest request = DealRequest.builder()
                            .dealUniqueId(requiredValue(record, DEAL_UNIQUE_ID))
                            .fromCurrencyIso(requiredValue(record, FROM_CURRENCY_ISO))
                            .toCurrencyIso(requiredValue(record, TO_CURRENCY_ISO))
                            .dealTimestamp(parseTimestamp(record))
                            .dealAmount(parseAmount(record))
                            .build();
                    rows.add(new DealCsvRow(rowNumber, request, null));
                } catch (IllegalArgumentException | DateTimeParseException ex) {
                    rows.add(new DealCsvRow(rowNumber, null, ex.getMessage()));
                }
            }
            return rows;
        } catch (IOException exception) {
            throw new InvalidCsvException("Failed to read CSV file.", exception);
        }
    }

    private void validateHeaders(CSVParser parser) {
        var headers = parser.getHeaderMap();
        if (headers == null || headers.isEmpty()) {
            throw new InvalidCsvException("CSV file must provide a header row.");
        }

        if (!(headers.containsKey(DEAL_UNIQUE_ID)
                && headers.containsKey(FROM_CURRENCY_ISO)
                && headers.containsKey(TO_CURRENCY_ISO)
                && headers.containsKey(DEAL_TIMESTAMP)
                && headers.containsKey(DEAL_AMOUNT))) {
            throw new InvalidCsvException(
                    "CSV file must contain headers: deal_unique_id, from_currency_iso, to_currency_iso, deal_timestamp, deal_amount.");
        }
    }

    private String requiredValue(CSVRecord record, String headerName) {
        String value = record.get(headerName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing value for '%s'".formatted(headerName));
        }
        return value.trim();
    }

    private Instant parseTimestamp(CSVRecord record) {
        return Instant.parse(requiredValue(record, DEAL_TIMESTAMP));
    }

    private BigDecimal parseAmount(CSVRecord record) {
        return new BigDecimal(requiredValue(record, DEAL_AMOUNT));
    }
}

