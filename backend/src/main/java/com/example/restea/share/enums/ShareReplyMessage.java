package com.example.restea.share.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShareReplyMessage {
    SHARE_REPLY_NOT_FOUND("ShareReply not found"),
    SHARE_REPLY_NO_CONTENT("No ShareReply"),
    SHARE_REPLY_NOT_WRITER("Not a writer."),
    SHARE_REPLY_NOT_ACTIVATED("ShareReply not activated");

    private final String message;
}
