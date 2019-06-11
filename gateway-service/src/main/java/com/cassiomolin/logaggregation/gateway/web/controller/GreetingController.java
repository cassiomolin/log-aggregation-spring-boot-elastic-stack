package com.cassiomolin.logaggregation.gateway.web.controller;

import com.cassiomolin.logaggregation.gateway.domain.Greeting;
import com.cassiomolin.logaggregation.gateway.service.GreetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/greetings")
public class GreetingController {

    private final GreetingService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Greeting> getGreeting() {
        Greeting greeting = service.getGreeting();
        return ResponseEntity.ok(greeting);
    }
}
