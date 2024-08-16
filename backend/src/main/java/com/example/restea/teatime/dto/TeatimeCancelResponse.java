package com.example.restea.teatime.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeCancelResponse {
    private final Integer boardId;
    private final Integer userId;

    public static TeatimeCancelResponse of(Integer boardId, Integer userId) {
        return TeatimeCancelResponse.builder()
                .boardId(boardId)
                .userId(userId)
                .build();
    }
}
