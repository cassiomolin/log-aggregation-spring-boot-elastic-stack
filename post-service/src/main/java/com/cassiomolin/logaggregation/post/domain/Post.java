package com.cassiomolin.logaggregation.post.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    protected Long id;

    protected String title;

    protected String content;

    protected OffsetDateTime publishDateTime;
}
