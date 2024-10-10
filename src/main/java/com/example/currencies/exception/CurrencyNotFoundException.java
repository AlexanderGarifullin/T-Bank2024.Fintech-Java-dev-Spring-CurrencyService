package com.example.currencies.exception;

import lombok.Getter;

@Getter
public class CurrencyNotFoundException extends RuntimeException {

    private final String code;

    public CurrencyNotFoundException(String code) {
        super("currency.not.found");
        this.code = code;
    }
}
