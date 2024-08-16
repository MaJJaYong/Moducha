package com.example.restea.share.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareCommentDeleteResponse {
    private final Integer commentId;

    public static ShareCommentDeleteResponse from(Integer commentId) {
        return ShareCommentDeleteResponse.builder()
                .commentId(commentId)
                .build();
    }
}
