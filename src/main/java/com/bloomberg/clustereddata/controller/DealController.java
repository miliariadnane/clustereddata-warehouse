package com.bloomberg.clustereddata.controller;

import com.bloomberg.clustereddata.dto.DealImportSummary;
import com.bloomberg.clustereddata.dto.DealRequest;
import com.bloomberg.clustereddata.dto.DealResponse;
import com.bloomberg.clustereddata.service.DealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deals")
public class DealController {

    private final DealService dealService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DealResponse createDeal(@Valid @RequestBody DealRequest dealRequest) {
        log.debug("Received deal creation request for {}", dealRequest.getDealUniqueId());
        return dealService.createDeal(dealRequest);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DealImportSummary importDeals(@RequestPart("file") MultipartFile file) {
        log.debug("Received CSV import request: {}", file.getOriginalFilename());
        return dealService.importDeals(file);
    }
}

