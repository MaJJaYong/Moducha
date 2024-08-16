package com.example.restea.live.dto;

import lombok.Builder;
import lombok.Getter;

// 티타임 방송 개설 여부 응답 정의하는 클래스
@Getter
@Builder
public class LiveIsOpenResponseDTO {
    private boolean isOpen;

    public static LiveIsOpenResponseDTO from(boolean isOpen) {
        return LiveIsOpenResponseDTO.builder()
                .isOpen(isOpen)
                .build();
    }
}
