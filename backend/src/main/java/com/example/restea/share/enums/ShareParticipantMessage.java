package com.example.restea.share.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShareParticipantMessage {
    SHARE_PARTICIPANT_NOT_FOUND("Share participant not found"),
    SHARE_PARTICIPANT_ALREADY_EXISTS("User already participating"),
    SHARE_PARTICIPANT_NOT_PARTICIPATING("User not participating"),
    SHARE_PARTICIPANT_NOT_OWNER("User is not the owner of the share board"),
    SHARE_PARTICIPANT_FULL("Share board already has maximum participants"),
    SHARE_PARTICIPANT_USER_IS_WRITER("User is the writer of the share board"),
    SHARE_PARTICIPANT_FORBIDDEN("User is not allowed to perform this action"),
    SHARE_PARTICIPANT_WRITER_DEACTIVATED("Writer of the share board is deactivated"),
    SHARE_PARTICIPANT_AFTER_END_DATE("After the end date.");

    private final String message;
}
