package com.example.currencies.controllers;

import com.example.currencies.dto.CurrencyConvertDTO;
import com.example.currencies.dto.CurrencyRateDTO;
import com.example.currencies.request.ConversionRequest;
import com.example.currencies.services.CurrencyService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/currencies")
@Validated
public class CurrencyRestController {

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyRestController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/rate/{code}")
    public CurrencyRateDTO getCurrencyRate(@PathVariable("code")
                                               @NotBlank(message = "currency.code.is_blank")
                                               @Pattern(message = "currency.code.invalid_format", regexp = "^[A-Z]{3}$")
                                               String code) {
        var rate = currencyService.getCurrencyRate(code);
        return new CurrencyRateDTO(code, rate);
    }

    @PostMapping("/convert")
    public CurrencyConvertDTO convertCurrency(@RequestBody ConversionRequest conversionRequest) {
        var convertedAmount = currencyService.convertToCurrency(conversionRequest.fromCurrency(),
                conversionRequest.toCurrency(), conversionRequest.amount());
        return new CurrencyConvertDTO(conversionRequest.fromCurrency(), conversionRequest.fromCurrency(), convertedAmount);
    }
}
