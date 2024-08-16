package com.example.restea.live.dto;

import io.livekit.server.AccessToken;
import lombok.Builder;
import lombok.Getter;

// 티타임 방송 생성, 참가 응답 정의하는 클래스
@Getter
@Builder
public class LiveRoomResponseDTO {
    private String token;

    public static LiveRoomResponseDTO from(AccessToken token) {
        return LiveRoomResponseDTO.builder()
                .token(token.toJwt())
                .build();
    }
}
