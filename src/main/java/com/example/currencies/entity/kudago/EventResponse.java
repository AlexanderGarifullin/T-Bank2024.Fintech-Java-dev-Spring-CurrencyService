package com.example.currencies.entity.kudago;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class EventResponse {

    private int id;

    private String title;

    private String price;

    @JsonProperty("is_free")
    private boolean free;

    @JsonProperty("dates")
    private List<DateResponse> dates;

    public boolean isHaveEnoughBudget(BigDecimal budget) {
        if (free) return true;
        if (price == null || price.isEmpty()) return false;

        BigDecimal cost = extractedPrice(price);

        if (cost == null) return false;
        return cost.compareTo(budget) <= 0;
    }

    private BigDecimal extractedPrice(String price) {
        if (price == null || price.isEmpty()) return null;
        return getLastNumber(price);
    }

    public static BigDecimal getLastNumber(String input) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        BigDecimal lastNumber = null;

        while (matcher.find()) {
            lastNumber = new BigDecimal(matcher.group());
        }

        return lastNumber;
    }
}
