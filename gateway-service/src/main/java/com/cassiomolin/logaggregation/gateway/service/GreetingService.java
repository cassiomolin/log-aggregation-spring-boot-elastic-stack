package com.cassiomolin.logaggregation.gateway.service;

import com.cassiomolin.logaggregation.gateway.domain.Greeting;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GreetingService {

    private final RestTemplate restTemplate;

    public Greeting getGreeting() {

        ResponseEntity<Greeting> responseEntity =
                restTemplate.getForEntity("http://localhost:8002/greetings", Greeting.class);

        return responseEntity.getBody();
    }
}
