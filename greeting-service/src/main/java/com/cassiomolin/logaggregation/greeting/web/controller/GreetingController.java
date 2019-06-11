package com.cassiomolin.logaggregation.greeting.web.controller;

import com.cassiomolin.logaggregation.greeting.service.GreetingService;
import com.cassiomolin.logaggregation.greeting.web.resource.GreetingResource;
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
    public ResponseEntity<GreetingResource> getGreeting() {

        String greeting = service.getGreeting();

        GreetingResource greetingResource = GreetingResource.builder()
                .greeting(greeting)
                .build();

        return ResponseEntity.ok(greetingResource);
    }
}
