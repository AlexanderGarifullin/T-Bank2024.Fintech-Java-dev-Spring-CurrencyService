package com.example.currencies.services;

import com.example.currencies.entity.kudago.EventResponse;
import com.example.currencies.entity.kudago.EventsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class KudaGoService {
    private static final Logger logger = LoggerFactory.getLogger(KudaGoService.class);
    private static final int PageSize = 100;
    private static final String TextFormat = "text";
    private static final String Fields = "id,title,price,is_free,dates";

    private final RestClient restClient;
    private final Semaphore rateLimiterSemaphore;

    @Value("${kudaGo.events}")
    private String getEventsUrl;

    @Autowired
    public KudaGoService(@Qualifier("restClientKudaGo") RestClient restClient,
                         @Qualifier("kudaGoRateLimiterSemaphore") Semaphore rateLimiterSemaphore) {
        this.restClient = restClient;
        this.rateLimiterSemaphore = rateLimiterSemaphore;
    }

    public CompletableFuture<List<EventResponse>> fetchEventsFuture(LocalDate dateFrom, LocalDate dateTo) {
        return CompletableFuture.supplyAsync(() -> {
            List<EventResponse> allEventResponses = new ArrayList<>();
            int page = 1;
            logger.info(String.format("Take events from %s to %s", dateFrom.toString(), dateTo.toString()));
            while (true) {
                logger.info("Get data from page " + page);

                try {
                    rateLimiterSemaphore.acquire();
                    EventsResponse eventsResponse = getEventsFromPageFuture(dateFrom, dateTo, page).join();
                    if (eventsResponse == null || eventsResponse.getResults().isEmpty()) {
                        break;
                    }
                    allEventResponses.addAll(eventsResponse.getResults());
                    page++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Request interrupted", e);
                } finally {
                    rateLimiterSemaphore.release();
                }
            }
            return allEventResponses;
        });
    }

    private CompletableFuture<EventsResponse> getEventsFromPageFuture(LocalDate dateFrom, LocalDate dateTo, int page) {
        return CompletableFuture.supplyAsync(() -> getEventsFromPage(dateFrom, dateTo, page));
    }

    public Mono<List<EventResponse>> fetchEventsReactive(LocalDate dateFrom, LocalDate dateTo) {
        List<EventResponse> allEventResponses = new ArrayList<>();
        AtomicInteger page = new AtomicInteger(1);

        logger.info(String.format("Take events from %s to %s", dateFrom.toString(), dateTo.toString()));

        return Mono.fromCallable(() -> {
            while (true) {
                logger.info("Get data from page " + page);
                try {
                    rateLimiterSemaphore.acquire();
                    EventsResponse eventsResponse = getEventsFromPage(dateFrom, dateTo, page.get());

                    if (eventsResponse == null || eventsResponse.getResults().isEmpty()) {
                        break;
                    }

                    logger.info("Add data from page " + page);
                    allEventResponses.addAll(eventsResponse.getResults());
                    page.getAndIncrement();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Request interrupted", e);
                } finally {
                    rateLimiterSemaphore.release();
                }
            }
            return allEventResponses;
        });
    }

    private EventsResponse getEventsFromPage(LocalDate dateFrom, LocalDate dateTo, int page) {
        try {
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(getEventsUrl)
                            .queryParam("actual_since", dateFrom.toString())
                            .queryParam("actual_until", dateTo.toString())
                            .queryParam("page", page)
                            .queryParam("page_size", PageSize)
                            .queryParam("text_format", TextFormat)
                            .queryParam("fields", Fields)
                            .build())
                    .retrieve()
                    .toEntity(EventsResponse.class);
            if (!(response.getStatusCode().is2xxSuccessful() && response.getBody() != null)) {
                logger.info("Get nothing from page" + page);
                return null;
            }
            logger.info("Get successfull from page" + page);
            logger.info("Cnt elements = " + response.getBody().getResults().size());
            return response.getBody();
        } catch (Exception ex) {
            logger.info("(ex) Get nothing from page" + page);
            logger.error(ex.getMessage());
            return null;
        }
    }
}

