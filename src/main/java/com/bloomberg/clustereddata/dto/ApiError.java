package com.bloomberg.clustereddata.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record ApiError(Instant timestamp, int status, String error, String message, String path, List<String> details) {
}

