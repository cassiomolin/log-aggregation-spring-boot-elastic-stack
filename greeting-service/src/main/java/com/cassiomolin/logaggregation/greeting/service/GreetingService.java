package com.cassiomolin.logaggregation.greeting.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String getGreeting() {
        return "Hey!";
    }
}
