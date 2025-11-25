package com.bloomberg.clustereddata.exception;

public class DealAlreadyExistsException extends RuntimeException {

    public DealAlreadyExistsException(String dealUniqueId) {
        super("Deal with id '%s' already exists".formatted(dealUniqueId));
    }

    public DealAlreadyExistsException(String dealUniqueId, Throwable cause) {
        super("Deal with id '%s' already exists".formatted(dealUniqueId), cause);
    }
}

