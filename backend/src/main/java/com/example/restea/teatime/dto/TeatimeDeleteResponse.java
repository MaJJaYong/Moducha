package com.example.restea.teatime.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeDeleteResponse {
    private final Integer boardId;

    public static TeatimeDeleteResponse from(Integer boardId) {
        return TeatimeDeleteResponse.builder()
                .boardId(boardId)
                .build();
    }
}
