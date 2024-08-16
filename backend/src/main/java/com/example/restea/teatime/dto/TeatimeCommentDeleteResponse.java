package com.example.restea.teatime.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeCommentDeleteResponse {
    private final Integer commentId;

    public static TeatimeCommentDeleteResponse from(Integer commentId) {
        return TeatimeCommentDeleteResponse.builder()
                .commentId(commentId)
                .build();
    }
}
