package com.example.restea.share.dto;

import com.example.restea.share.entity.ShareReply;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareReplyViewResponse {
    private final Integer commentId;
    private final Integer replyId;
    private final String content;
    private final LocalDateTime createdDate;
    private final Integer userId;
    private final String nickname;
    private final String picture;

    public static ShareReplyViewResponse of(ShareReply shareReply) {
        return ShareReplyViewResponse.builder()
                .commentId(shareReply.getShareComment().getId())
                .replyId(shareReply.getId())
                .content(shareReply.getExposedContent())
                .createdDate(shareReply.getCreatedDate())
                .userId(shareReply.getUser().getId())
                .nickname(shareReply.getExposedNickname())
                .picture(shareReply.getUser().getPicture())
                .build();
    }

}
