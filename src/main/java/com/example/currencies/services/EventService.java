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

/**
 * Service class responsible for handling event-related business logic.
 * This service filters events based on a specified budget and time range.
 */
@Service
public class EventService {

    private static final int PlusDayCnt = 7;
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);


    private final KudaGoService kudaGoService;
    private final CurrencyService currencyService;

    /**
     * Constructs an EventService with specified dependencies.
     *
     * @param kudaGoService   service for retrieving events from KudaGo API
     * @param currencyService service for handling currency conversion operations
     */
    @Autowired
    public EventService(KudaGoService kudaGoService, CurrencyService currencyService) {
        this.kudaGoService = kudaGoService;
        this.currencyService = currencyService;
    }

    /**
     * Asynchronously retrieves and filters events that are within the specified budget and date range.
     *
     * @param budget   the budget constraint for the events
     * @param currency the currency in which the budget is specified
     * @param dateFrom the start date for filtering events
     * @param dateTo   the end date for filtering events
     * @return a CompletableFuture containing a list of events matching the budget and date criteria
     */
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

    /**
     * Reactively retrieves and filters events that are within the specified budget and date range.
     *
     * @param budget   the budget constraint for the events
     * @param currency the currency in which the budget is specified
     * @param dateFrom the start date for filtering events
     * @param dateTo   the end date for filtering events
     * @return a Mono containing a list of events matching the budget and date criteria
     */
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

    /**
     * Returns the provided date if it is not null; otherwise, returns today's date.
     *
     * @param date the date to check
     * @return the provided date if not null; otherwise, today's date
     */
    private LocalDate checkData(LocalDate date) {
        return checkData(date, 0);
    }

    /**
     * Checks and provides a default date if null, with an optional offset in days.
     *
     * @param date       the date to check
     * @param plusDayCnt the number of days to add if date is null
     * @return the given date if not null; otherwise, today's date plus the specified offset
     */
    private LocalDate checkData(LocalDate date, int plusDayCnt) {
        return (date != null) ? date : LocalDate.now().plusDays(plusDayCnt);
    }

    /**
     * Logs information about the budget and the number of retrieved events.
     *
     * @param convertedBudget the converted budget in rubles
     * @param events          the list of events that were retrieved
     */
    private void logFlowInfo(BigDecimal convertedBudget, List<EventResponse> events) {
        logger.info("BUDGET = " + convertedBudget);
        logger.info("GET ELEMENTS SIZE = " + events.size());
    }
}
