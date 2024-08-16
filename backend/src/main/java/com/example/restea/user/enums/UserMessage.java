package com.example.restea.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserMessage {

    USER_NOT_FOUND("유저 정보가 없습니다."),
    USER_ALREADY_WITHDRAWN("이미 탈퇴한 유저입니다."),
    USER_INVALID("올바르지 않은 유저입니다."),
    USER_NICKNAME_SAME("변경 전과 동일한 닉네임입니다."),
    USER_NICKNAME_EXISTS("이미 존재하는 닉네임입니다."),

    USER_NOT_ACTIVATED("User not activated.");

    private final String message;
}
