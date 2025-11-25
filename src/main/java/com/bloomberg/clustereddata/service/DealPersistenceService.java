package com.bloomberg.clustereddata.service;

import com.bloomberg.clustereddata.domain.Deal;
import com.bloomberg.clustereddata.exception.DealAlreadyExistsException;
import com.bloomberg.clustereddata.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DealPersistenceService {

    private final DealRepository dealRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Deal save(Deal deal) {
        try {
            return dealRepository.save(deal);
        } catch (DataIntegrityViolationException exception) {
            throw new DealAlreadyExistsException(deal.getDealUniqueId(), exception);
        }
    }
}

