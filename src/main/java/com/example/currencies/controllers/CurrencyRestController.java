package com.example.currencies.controllers;

import com.example.currencies.dto.CurrencyConvertDTO;
import com.example.currencies.dto.CurrencyRateDTO;
import com.example.currencies.request.ConversionRequest;
import com.example.currencies.services.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
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

    @Operation(summary = "Get currency rate",
            description = "Returns the exchange rate of a currency against the Russian ruble.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved currency rate",
                            content = @Content(schema = @Schema(implementation = CurrencyRateDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid currency code",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Currency not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "503", description = "Currency service is unavailable",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/rate/{code}")
    public CurrencyRateDTO getCurrencyRate(@PathVariable("code")
                                               @NotBlank(message = "currency.code.is_blank")
                                               @Pattern(message = "currency.code.invalid_format", regexp = "^[A-Z]{3}$")
                                               String code) {
        var rate = currencyService.getCurrencyRate(code);
        return new CurrencyRateDTO(code, rate);
    }

    @PostMapping("/convert")
    @Operation(summary = "Convert currency",
            description = "Converts an amount from one currency to another.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Currency successfully converted",
                            content = @Content(schema = @Schema(implementation = CurrencyConvertDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid currency codes or amount",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "One of the currencies was not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "503", description = "Currency service is unavailable",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public CurrencyConvertDTO convertCurrency(@RequestBody ConversionRequest conversionRequest) {
        var convertedAmount = currencyService.convertToCurrency(conversionRequest.fromCurrency(),
                conversionRequest.toCurrency(), conversionRequest.amount());
        return new CurrencyConvertDTO(conversionRequest.fromCurrency(), conversionRequest.toCurrency(), convertedAmount);
    }
}
