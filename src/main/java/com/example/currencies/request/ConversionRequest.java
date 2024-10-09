package com.example.currencies.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ConversionRequest (
        @NotBlank(message = "currency.code.is_blank")
        @Pattern(message = "currency.code.invalid_format", regexp = "^[A-Z]{3}$")
        String fromCurrency,

        @NotBlank(message = "currency.code.is_blank")
        @Pattern(message = "currency.code.invalid_format", regexp = "^[A-Z]{3}$")
        String toCurrency,

        @NotNull(message = "currency.amount.is_null")
        @Positive(message = "currency.amount.should_be_positive")
        BigDecimal amount
){}
