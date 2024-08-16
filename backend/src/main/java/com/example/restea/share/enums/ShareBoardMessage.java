package com.example.restea.share.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShareBoardMessage {
    SHARE_BOARD_NOT_FOUND("ShareBoard not found"),
    SHARE_BOARD_USER_NOT_ACTIVATED("ShareBoard User not activated"),
    SHARE_BOARD_NOT_WRITER("Not a writer."),
    SHARE_BOARD_LESS_THAN_CURRENT_PARTICIPANTS("Less than current participants."),
    SHARE_BOARD_INVALID_SORT("Invalid sort value."),
    SHARE_BOARD_INVALID_SEARCH_BY("Invalid searchBy value."),
    SHARE_BOARD_INVALID_KEYWORD("Invalid keyword value.");

    private final String message;
}
