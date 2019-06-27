package com.cassiomolin.logaggregation.post.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private Long id;

    private String title;

    private Integer year;

    private List<Comment> comments;
}
