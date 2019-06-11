package com.cassiomolin.logaggregation.gateway.service;

import com.cassiomolin.logaggregation.gateway.domain.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GreetingService {

    private final RestTemplate restTemplate;

    public Greeting getGreeting() {

        log.info("Requesting greeting");

        ResponseEntity<Greeting> responseEntity =
                restTemplate.getForEntity("http://localhost:8002/greetings", Greeting.class);

        log.info("Found greeting");

        return responseEntity.getBody();
    }
}
