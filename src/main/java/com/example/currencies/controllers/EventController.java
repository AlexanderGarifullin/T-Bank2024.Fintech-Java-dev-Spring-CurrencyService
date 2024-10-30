package com.example.currencies.controllers;

import com.example.currencies.entity.kudago.EventResponse;
import com.example.currencies.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;


    @Operation(
            summary = "Get events based on user preferences",
            description = "Returns a list of popular events within the specified period and budget in the requested currency.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved events",
                            content = @Content(schema = @Schema(implementation = EventResponse.class))),
            }
    )
    @GetMapping("/completableFuture")
    public CompletableFuture<List<EventResponse>> getEvents(
            @RequestParam("budget")
            @DecimalMin(value = "0.0", inclusive = false, message = "currency.budget.should_be_positive") BigDecimal budget,

            @RequestParam("currency")
            @Pattern(message = "currency.code.invalid_format", regexp = "^[A-Z]{3}$") String currency,

            @RequestParam(value = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate dateFrom,

            @RequestParam(value = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return eventService.fetchEventsFuture(budget, currency, dateFrom, dateTo);
    }


    @Operation(
            summary = "Get events based on user preferences",
            description = "Returns a list of popular events within the specified period and budget in the requested currency.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved events",
                            content = @Content(schema = @Schema(implementation = EventResponse.class))),
            }
    )
    @GetMapping("/reactive")
    public Mono<List<EventResponse>> getReactiveEvents(
            @RequestParam("budget")
            @DecimalMin(value = "0.0", inclusive = false, message = "currency.budget.should_be_positive") BigDecimal budget,

            @RequestParam("currency")
            @Pattern(message = "currency.code.invalid_format", regexp = "^[A-Z]{3}$") String currency,

            @RequestParam(value = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate dateFrom,

            @RequestParam(value = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return eventService.fetchEventsReactive(budget, currency, dateFrom, dateTo);
    }
}
