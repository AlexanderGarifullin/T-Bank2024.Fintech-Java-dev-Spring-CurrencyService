package com.example.currencies.entity.kudago;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EventsResponse
{
    private final List<EventResponse> results = new ArrayList<>();
    private int count;
    private String next;
    private String previous;
}
