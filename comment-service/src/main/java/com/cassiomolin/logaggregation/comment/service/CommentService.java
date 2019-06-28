package com.cassiomolin.logaggregation.comment.service;

import com.cassiomolin.logaggregation.comment.domain.Comment;

import java.util.List;

public interface CommentService {

    List<Comment> getCommentsForPost(Long postId);
}
