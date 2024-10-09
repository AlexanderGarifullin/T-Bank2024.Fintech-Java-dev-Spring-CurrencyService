package com.example.currencies.controllers;

import com.example.currencies.dto.CurrencyConvertDTO;
import com.example.currencies.dto.CurrencyRateDTO;
import com.example.currencies.request.ConversionRequest;
import com.example.currencies.services.CurrencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyRestControllerTest {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyRestController currencyRestController;

    @Test
    public void testGetCurrencyRate() {
        String code = "USD";
        BigDecimal rate = BigDecimal.valueOf(96.9483);
        when(currencyService.getCurrencyRate(code)).thenReturn(rate);

        CurrencyRateDTO response = currencyRestController.getCurrencyRate(code);

        assertThat(response).isEqualTo(new CurrencyRateDTO(code, rate));
    }

    @Test
    public void testConvertCurrency() {
        ConversionRequest conversionRequest = new ConversionRequest("USD", "EUR", BigDecimal.valueOf(100));
        BigDecimal convertedAmount = BigDecimal.valueOf(90.0);
        when(currencyService.convertToCurrency("USD", "EUR", BigDecimal.valueOf(100))).thenReturn(convertedAmount);

        CurrencyConvertDTO response = currencyRestController.convertCurrency(conversionRequest);

        assertThat(response).isEqualTo(new CurrencyConvertDTO("USD", "EUR", convertedAmount));
    }

}