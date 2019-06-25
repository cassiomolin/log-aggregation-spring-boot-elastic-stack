package com.cassiomolin.logaggregation.movie.web.controller;

import com.cassiomolin.logaggregation.movie.domain.Movie;
import com.cassiomolin.logaggregation.movie.service.MovieService;
import com.cassiomolin.logaggregation.movie.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/movies")
public class MovieController {

    private final MovieService service;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {
        Movie movie = service.getMovie(id).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(movie);
    }
}
