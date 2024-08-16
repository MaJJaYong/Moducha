package com.example.restea.teatime.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TeatimeBoardMessage {
    TEATIME_BOARD_NOT_FOUND("TeatimeBoard not found."),
    TEATIME_BOARD_NOT_ACTIVATED("TeatimeBoard not activated."),
    TEATIME_BOARD_NOT_WRITER("Not a writer."),
    TEATIME_BOARD_NOT_BROADCAST_DATE("Different from the broadcast date."),
    TEATIME_BOARD_BEFORE_BROADCAST_DATE("Before 30minutes the broadcast date."),
    TEATIME_BOARD_AFTER_BROADCAST_DATE("After 30minutes the broadcast date."),
    TEATIME_BOARD_INVALID_SORT("Invalid sort."),
    TEATIME_BOARD_LESS_THAN_CURRENT_PARTICIPANTS("Less than current participants."),
    TEATIME_BOARD_USER_NOT_ACTIVATED("TeatimeBoard User not activated"),
    TEATIME_BOARD_WRITER("TeatimeBoard Writer"),
    TEATIME_BOARD_INVALID_SEARCH_BY("Invalid search by.");

    private final String message;
}