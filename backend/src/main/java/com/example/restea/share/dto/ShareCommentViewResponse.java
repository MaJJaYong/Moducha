package com.example.restea.share.dto;

import com.example.restea.share.entity.ShareComment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareCommentViewResponse {
    private final Integer commentId;
    private final Integer boardId;
    private final String content;
    private final LocalDateTime createdDate;
    private final Integer userId;
    private final String nickname;
    private final Integer replyCount;
    private final String picture;

    public static ShareCommentViewResponse of(ShareComment shareComment, Integer replyCount) {
        return ShareCommentViewResponse.builder()
                .commentId(shareComment.getId())
                .boardId(shareComment.getShareBoard().getId())
                .content(shareComment.getExposedContent())
                .createdDate(shareComment.getCreatedDate())
                .userId(shareComment.getUser().getId())
                .nickname(shareComment.getExposedNickname())
                .replyCount(replyCount)
                .picture(shareComment.getUser().getPicture())
                .build();
    }
}