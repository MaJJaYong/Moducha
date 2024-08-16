package com.example.restea.share.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareJoinCheckResponse {
    private Integer boardId;
    private Integer userId;
    private boolean participated;

    public static ShareJoinCheckResponse of(Integer boardId, Integer userId, boolean participated) {
        return ShareJoinCheckResponse.builder()
                .boardId(boardId)
                .userId(userId)
                .participated(participated)
                .build();
    }
}