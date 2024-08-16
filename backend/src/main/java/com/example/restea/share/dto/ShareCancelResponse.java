package com.example.restea.share.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareCancelResponse {
    private Integer boardId;
    private Integer userId;

    public static ShareCancelResponse of(Integer boardId, Integer userId) {
        return ShareCancelResponse.builder()
                .boardId(boardId)
                .userId(userId)
                .build();
    }
}