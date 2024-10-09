package com.example.currencies.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final CBRService cbrService;

    @Autowired
    public DataLoader(CBRService cbrService) {
        this.cbrService = cbrService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent() {
        cbrService.getValuta().ifPresentOrElse(
                valuta -> logger.info("Uploaded {} currencies", valuta.getItems().size())
                , () -> logger.warn("Currency data has not been uploaded!"));
        cbrService.getValCurs().ifPresentOrElse(
                valCurs -> logger.info("Uploaded {} rates", valCurs.getValutes().size())
                , () -> logger.warn("Currency exchange rate data has not been uploaded!"));
    };
}
