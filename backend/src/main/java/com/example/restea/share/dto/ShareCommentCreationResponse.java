package com.example.restea.share.dto;

import com.example.restea.share.entity.ShareComment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareCommentCreationResponse {
    private final Integer commentId;
    private final Integer boardId;
    private final String content;
    private final LocalDateTime createdDate;

    public static ShareCommentCreationResponse of(ShareComment shareComment) {
        return ShareCommentCreationResponse.builder()
                .commentId(shareComment.getId())
                .boardId(shareComment.getShareBoard().getId())
                .content(shareComment.getContent())
                .createdDate(shareComment.getCreatedDate())
                .build();
    }
}
