package com.incubyte.salary.analytics.service;

import com.incubyte.salary.common.exception.UnsupportedCurrencyException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateConfigTest {

    private final ExchangeRateConfig exchangeRateConfig = new ExchangeRateConfig();

    @Test
    void leavesUsdAmountsUnchanged() {
        BigDecimal result = exchangeRateConfig.toReportingCurrency(new BigDecimal("1000.00"), "USD");

        assertThat(result).isEqualByComparingTo("1000.00");
    }

    @Test
    void convertsGbpToUsdUsingTheConfiguredRate() {
        BigDecimal result = exchangeRateConfig.toReportingCurrency(new BigDecimal("80000.00"), "GBP");

        assertThat(result).isEqualByComparingTo("100000.00");
    }

    @Test
    void convertsInrToUsdUsingTheConfiguredRate() {
        BigDecimal result = exchangeRateConfig.toReportingCurrency(new BigDecimal("830000.00"), "INR");

        assertThat(result).isEqualByComparingTo("10000.00");
    }

    @Test
    void rejectsAnUnconfiguredCurrency() {
        assertThatThrownBy(() -> exchangeRateConfig.toReportingCurrency(BigDecimal.TEN, "JPY"))
                .isInstanceOf(UnsupportedCurrencyException.class);
    }
}
