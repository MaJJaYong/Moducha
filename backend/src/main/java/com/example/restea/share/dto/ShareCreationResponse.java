package com.example.restea.share.dto;

import com.example.restea.share.entity.ShareBoard;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class ShareCreationResponse {

    //  "shareBoardId": 1,
//  "title": "New Post Title",
//  "content": "This is the content of the new post.",
//  "createdDate": "2023-07-15T10:00:00Z",
//  "endDate": "2023-08-01T12:00:00Z",
//  "maxParticipants": 10,

    private final Integer boardId;
    private final String title;
    private final String content;
    private final LocalDateTime createdDate;
    private final LocalDateTime endDate;
    private final Integer maxParticipants;

    public static ShareCreationResponse of(@NotNull ShareBoard shareBoard) {
        return ShareCreationResponse.builder()
                .boardId(shareBoard.getId())
                .title(shareBoard.getTitle())
                .content(shareBoard.getContent())
                .createdDate(shareBoard.getCreatedDate())
                .endDate(shareBoard.getEndDate())
                .maxParticipants(shareBoard.getMaxParticipants())
                .build();
    }

    @Builder
    public ShareCreationResponse(Integer boardId, String title, String content,
                                 LocalDateTime createdDate, LocalDateTime endDate,
                                 Integer maxParticipants) {
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.endDate = endDate;
        this.maxParticipants = maxParticipants;
    }

}
