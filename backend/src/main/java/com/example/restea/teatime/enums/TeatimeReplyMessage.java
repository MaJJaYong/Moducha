package com.example.restea.teatime.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TeatimeReplyMessage {
    TEATIME_REPLY_NOT_FOUND("TeatimeReply not found"),
    TEATIME_REPLY_NOT_WRITER("Not a writer.");

    private final String message;
}
