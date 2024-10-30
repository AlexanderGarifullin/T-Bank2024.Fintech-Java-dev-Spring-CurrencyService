package com.example.currencies.services;

import com.example.currencies.entity.kudago.EventResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final KudaGoService kudaGoService;
    private final CurrencyService currencyService;

    @Autowired
    public EventService(KudaGoService kudaGoService, CurrencyService currencyService) {
        this.kudaGoService = kudaGoService;
        this.currencyService = currencyService;
    }

    @Async("asyncExecutor")
    public CompletableFuture<List<EventResponse>> fetchEventsFuture(BigDecimal budget, String currency, LocalDate dateFrom,
                                                                    LocalDate dateTo) {
        dateFrom = checkData(dateFrom);
        dateTo = checkData(dateTo, 7);
        var convertedBudgetFuture =currencyService.convertBudgetToRublesFuture(currency, budget);
        var eventsFuture = kudaGoService.fetchEventsFuture(dateFrom, dateTo);
        var resultFuture = new CompletableFuture<List<EventResponse>>();

        convertedBudgetFuture.thenAcceptBoth(eventsFuture, (convertedBudget, events) -> {
            List<EventResponse> filteredEventResponses = events.stream()
                    .filter(event -> event.isHaveEnoughBudget(convertedBudget))
                    .collect(Collectors.toList());
            resultFuture.complete(filteredEventResponses);
        }).exceptionally(ex -> {
            resultFuture.completeExceptionally(ex);
            return null;
        });

        return resultFuture;
    }

    private LocalDate checkData(LocalDate date) {
        return checkData(date, 0);
    }

    private LocalDate checkData(LocalDate date, int plusDayCnt) {
        return (date != null) ? date : LocalDate.now().plusDays(plusDayCnt);
    }
}
