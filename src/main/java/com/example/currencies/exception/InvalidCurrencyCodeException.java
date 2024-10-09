package com.example.currencies.exception;

import lombok.Getter;

@Getter
public class InvalidCurrencyCodeException extends RuntimeException {

  private final String code;

  public InvalidCurrencyCodeException(String code) {
        super("invalid currency code");
        this.code = code;
    }
}
