package com.example.restea.share.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShareCommentMessage {
    SHARE_COMMENT_NOT_FOUND("ShareComment not found"),
    SHARE_COMMENT_NO_CONTENT("No ShareComment"),
    SHARE_COMMENT_NOT_WRITER("Not a writer."),
    SHARE_COMMENT_NOT_ACTIVATED("ShareComment not activated");

    private final String message;
}
