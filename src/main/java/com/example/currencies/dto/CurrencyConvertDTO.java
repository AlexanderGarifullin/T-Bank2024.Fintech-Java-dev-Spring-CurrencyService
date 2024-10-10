package com.example.currencies.dto;

import java.math.BigDecimal;

public record CurrencyConvertDTO (
    String fromCurrency,

    String toCurrency,

    BigDecimal convertedAmount
) {}