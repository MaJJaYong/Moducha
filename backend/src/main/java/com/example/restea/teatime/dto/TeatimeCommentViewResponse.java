package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeComment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeCommentViewResponse {
    private final Integer commentId;
    private final Integer boardId;
    private final String content;
    private final LocalDateTime createdDate;
    private final Integer userId;
    private final String nickname;
    private final Integer replyCount;
    private final String picture;

    public static TeatimeCommentViewResponse of(TeatimeComment teatimeComment, Integer replyCount) {
        return TeatimeCommentViewResponse.builder()
                .commentId(teatimeComment.getId())
                .boardId(teatimeComment.getTeatimeBoard().getId())
                .content(teatimeComment.getExposedContent())
                .createdDate(teatimeComment.getCreatedDate())
                .userId(teatimeComment.getUser().getId())
                .nickname(teatimeComment.getExposedNickname())
                .replyCount(replyCount)
                .picture(teatimeComment.getUser().getPicture())
                .build();
    }
}
