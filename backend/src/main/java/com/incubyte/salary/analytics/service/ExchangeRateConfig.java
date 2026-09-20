package com.incubyte.salary.analytics.service;

import com.incubyte.salary.common.exception.UnsupportedCurrencyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Static, documented exchange-rate configuration used to normalize salaries
 * into one reporting currency for cross-country/department analytics. Rates
 * are units of the given currency per 1 USD (e.g. 83 INR = 1 USD).
 * Intentionally not live/dynamic - see docs/assumptions.md #7.
 */
@Component
public class ExchangeRateConfig {

    public static final String REPORTING_CURRENCY = "USD";

    private static final Map<String, BigDecimal> UNITS_PER_USD = Map.ofEntries(
            Map.entry("USD", BigDecimal.ONE),
            Map.entry("GBP", new BigDecimal("0.80")),
            Map.entry("EUR", new BigDecimal("0.90")),
            Map.entry("INR", new BigDecimal("83.00")),
            Map.entry("CAD", new BigDecimal("1.35")),
            Map.entry("AUD", new BigDecimal("1.50")),
            Map.entry("SGD", new BigDecimal("1.30")),
            Map.entry("BRL", new BigDecimal("5.30"))
    );

    public BigDecimal toReportingCurrency(BigDecimal amount, String currency) {
        BigDecimal unitsPerUsd = UNITS_PER_USD.get(currency);
        if (unitsPerUsd == null) {
            throw new UnsupportedCurrencyException(currency);
        }
        return amount.divide(unitsPerUsd, 2, RoundingMode.HALF_UP);
    }
}
