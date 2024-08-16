package com.example.restea.teatime.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeJoinCheckResponse {
    private final Integer boardId;
    private final Integer userId;
    private final Boolean participated;

    public static TeatimeJoinCheckResponse of(Integer boardId, Integer userId, Boolean participated) {
        return TeatimeJoinCheckResponse.builder()
                .boardId(boardId)
                .userId(userId)
                .participated(participated)
                .build();
    }
}
