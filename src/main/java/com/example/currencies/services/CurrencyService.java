package com.example.currencies.services;

import com.example.currencies.entity.ValCurs;
import com.example.currencies.exception.CurrencyNotFoundException;
import com.example.currencies.exception.InvalidCurrencyCodeException;
import com.example.currencies.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyService {

    private final CBRService cbrService;

    @Autowired
    public CurrencyService(CBRService cbrService) {
        this.cbrService = cbrService;
    }

    public BigDecimal getCurrencyRate(String currencyCode) {
        var currencies = cbrService.getValCurs().orElseThrow(ServiceUnavailableException::new);

        validate(currencyCode);

        return getCurrencyRateFromList(currencies, currencyCode);
    }

    public BigDecimal convertToCurrency(String fromCurrency, String toCurrency, BigDecimal amount) {
        var currencies = cbrService.getValCurs().orElseThrow(ServiceUnavailableException::new);

        validate(fromCurrency);
        validate(toCurrency);

        var fromCurrencyRate = getCurrencyRateFromList(currencies, fromCurrency);
        var toCurrencyRate = getCurrencyRateFromList(currencies, toCurrency);

        return amount.multiply(fromCurrencyRate).divide(toCurrencyRate, RoundingMode.HALF_UP);
    }

    private void validate(String code) {
        var currency = cbrService.getValuta().orElseThrow(ServiceUnavailableException::new);

        currency.getItems().stream()
                .filter(item -> item.getIsoCharCode().equals(code))
                .findFirst()
                .orElseThrow(() ->
                        new InvalidCurrencyCodeException(code));
    }

    private BigDecimal getCurrencyRateFromList(ValCurs currencies, String code) {
        return currencies.getValutes().stream()
                .filter(item -> item.getCharCode().equals(code))
                .findFirst().orElseThrow(
                        () -> new CurrencyNotFoundException(code)
                )
                .getVunitRate();
    };
}
