package com.example.restea.live.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LiveMessage {
    LIVE_NOT_FOUND("Live not found."),
    LIVEKIT_BAD_REQUEST("Livekit error.");

    private final String message;
}
