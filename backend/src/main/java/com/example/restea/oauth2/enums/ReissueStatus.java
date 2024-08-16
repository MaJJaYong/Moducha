package com.example.restea.oauth2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReissueStatus {
    REFRESH_VALID("올바른 Refresh Token입니다."),
    REFRESH_NULL("Refresh Token이 없습니다."),
    REFRESH_EXPIRED("Refresh Token이 만료되었습니다."),
    REFRESH_INVALID("올바르지 않은 Refresh Token 입니다.");

    private final String message;
}