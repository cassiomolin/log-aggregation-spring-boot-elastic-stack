package com.cassiomolin.logaggregation.greeting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GreetingService {

    public String getGreeting() {
        log.info("Returning greeting");
        return "Hey!";
    }
}
