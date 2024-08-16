package com.example.restea.share.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ShareDeleteResponse {
    private final Integer boardId;

    @Builder
    public ShareDeleteResponse(Integer boardId) {
        this.boardId = boardId;
    }
}
