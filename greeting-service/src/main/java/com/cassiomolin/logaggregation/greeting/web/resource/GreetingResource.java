package com.cassiomolin.logaggregation.greeting.web.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GreetingResource {

    private String greeting;
}
