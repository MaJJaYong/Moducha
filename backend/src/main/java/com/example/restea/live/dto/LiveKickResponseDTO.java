package com.example.restea.live.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiveKickResponseDTO {
    private Integer userId;

    public static LiveKickResponseDTO from(Integer userId) {
        return LiveKickResponseDTO.builder()
                .userId(userId)
                .build();
    }
}
