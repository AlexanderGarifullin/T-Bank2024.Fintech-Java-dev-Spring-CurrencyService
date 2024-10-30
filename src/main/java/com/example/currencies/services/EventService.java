package com.example.currencies.services;

import com.example.currencies.entity.kudago.EventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class EventService {

    private static final int PlusDayCnt = 7;
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);


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
        dateTo = checkData(dateTo, PlusDayCnt);
        var convertedBudgetFuture =currencyService.convertBudgetToRublesFuture(currency, budget);
        var eventsFuture = kudaGoService.fetchEventsFuture(dateFrom, dateTo);
        var resultFuture = new CompletableFuture<List<EventResponse>>();

        convertedBudgetFuture.thenAcceptBoth(eventsFuture, (convertedBudget, events) -> {
            logFlowInfo(convertedBudget, events);

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

    public Mono<List<EventResponse>> fetchEventsReactive(BigDecimal budget, String currency, LocalDate dateFrom,
                                                       LocalDate dateTo) {
        dateFrom = checkData(dateFrom);
        dateTo = checkData(dateTo, 7);

        Mono<BigDecimal> convertedBudgetMono = currencyService.convertBudgetToRublesReactive(currency, budget);
        Mono<List<EventResponse>> eventsMono = kudaGoService.fetchEventsReactive(dateFrom, dateTo);

        return Mono.zip(convertedBudgetMono, eventsMono)
                .flatMap(tuple -> {
                    BigDecimal convertedBudget = tuple.getT1();
                    List<EventResponse> events = tuple.getT2();
                    logFlowInfo(convertedBudget, events);

                    List<EventResponse> filteredEventResponses = events.stream()
                            .filter(event -> event.isHaveEnoughBudget(convertedBudget))
                            .toList();


                    return Mono.just(filteredEventResponses);
                })
                .onErrorResume(ex -> {
                    logger.error(ex.getMessage());
                    return Mono.error(ex);
                });
    }

    private LocalDate checkData(LocalDate date) {
        return checkData(date, 0);
    }

    private LocalDate checkData(LocalDate date, int plusDayCnt) {
        return (date != null) ? date : LocalDate.now().plusDays(plusDayCnt);
    }


    private void logFlowInfo(BigDecimal convertedBudget, List<EventResponse> events) {
        logger.info("BUDGET = " + convertedBudget);
        logger.info("GET ELEMENTS SIZE = " + events.size());
    }
}
