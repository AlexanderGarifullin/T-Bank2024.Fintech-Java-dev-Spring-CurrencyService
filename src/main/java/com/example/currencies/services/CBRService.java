package com.example.currencies.services;

import com.example.currencies.entity.cbr.Item;
import com.example.currencies.entity.cbr.ValCurs;
import com.example.currencies.entity.cbr.Valuta;
import com.example.currencies.entity.cbr.Valute;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CBRService {

    private static final Logger logger = LoggerFactory.getLogger(CBRService.class);

    private final RestClient restClient;

    @Value("${cbr.daily}")
    private String getValCursUrl;

    @Value("${cbr.valFull}")
    private String getValutaUrl;

    @Autowired
    public CBRService(@Qualifier("restClientCBR") RestClient restClient) {
        this.restClient = restClient;
    }

    @Cacheable(value = "valutaCache")
    @CircuitBreaker(name = "cbrValuta", fallbackMethod = "getValutaFallback")
    public Optional<Valuta> getValuta() {
        var response = restClient.get()
                .uri(getValutaUrl)
                .retrieve()
                .toEntity(Valuta.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            response.getBody().getItems().add(getRub());
        }

        logger.info("GetValuta respone = {}", response);

        return Optional.ofNullable(response.getBody());
    }

    public Optional<Valuta> getValutaFallback(Exception ex) {
        logger.error(getFallbackExMsg("getValuta", ex));
        return Optional.empty();
    }

    @Cacheable(value = "valCursCache")
    @CircuitBreaker(name = "cbrValCurs", fallbackMethod = "getValCursFallback")
    public Optional<ValCurs> getValCurs() {
        var response = restClient.get()
                .uri(getValCursUrl)
                .retrieve()
                .toEntity(ValCurs.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            response.getBody().getValutes().add(getRubCurs());
        }

        logger.info("getValCurs respone = {}", response);
        return Optional.ofNullable(response.getBody());
    }

    public Optional<Valuta> getValCursFallback(Exception ex) {
        logger.error(getFallbackExMsg("getValCurs", ex));
        return Optional.empty();
    }

    private String getFallbackExMsg(String method, Exception ex) {
        return String.format("Circuit breaker fallback for %s. Error: %s", method, ex.getMessage());
    }

    private Item getRub() {
        return new Item(
                "R01235",
                "Российский рубль",
                "Russian Ruble",
                1,
                "R01235",
                643,
                "RUB"
        );
    }

    private Valute getRubCurs() {
        return new Valute(
                "R01235",
                643,
                "RUB",
                1,
                "Российский рубль",
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(1.0)
        );
    }
}
