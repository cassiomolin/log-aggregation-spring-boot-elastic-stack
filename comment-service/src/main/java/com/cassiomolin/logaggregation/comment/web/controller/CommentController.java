package com.cassiomolin.logaggregation.comment.web.controller;

import com.cassiomolin.logaggregation.comment.domain.Comment;
import com.cassiomolin.logaggregation.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Comment>> getCommentsForPost(@RequestParam Long postId) {
        List<Comment> comments = service.getCommentsForPost(postId);
        return ResponseEntity.ok(comments);
    }
}
