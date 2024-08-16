package com.example.restea.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NicknameUpdateResponse {
    private final String nickname;

    public static NicknameUpdateResponse from(String nickname) {
        return NicknameUpdateResponse.builder()
                .nickname(nickname)
                .build();
    }
}
