package com.example.restea.live.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiveMuteResponseDTO {
    private Integer userId;
    private String trackSid;

    public static LiveMuteResponseDTO of(LiveMuteRequestDTO request) {
        return LiveMuteResponseDTO.builder()
                .userId(request.getUserId())
                .trackSid(request.getTrackSid())
                .build();
    }
}
