package com.example.restea.teatime.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TeatimeCommentMessage {
    TEATIME_COMMENT_NO_CONTENT("No TeatimeComment"),
    TEATIME_COMMENT_NOT_WRITER("Not a writer."),
    TEATIME_COMMENT_NOT_FOUND("TeatimeComment not found");

    private final String message;
}
