package com.incubyte.salary.common.exception;

public class UnsupportedCurrencyException extends RuntimeException {

    public UnsupportedCurrencyException(String currency) {
        super("No exchange rate configured for currency: " + currency);
    }
}
