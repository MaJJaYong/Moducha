package com.example.restea.share.dto;

import com.example.restea.share.entity.ShareReply;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareReplyCreationResponse {
    private final Integer commentId;
    private final Integer replyId;
    private final String content;
    private final String nickname;

    public static ShareReplyCreationResponse of(ShareReply shareReply, String nickname) {
        return ShareReplyCreationResponse.builder()
                .commentId(shareReply.getShareComment().getId())
                .replyId(shareReply.getId())
                .content(shareReply.getContent())
                .nickname(nickname)
                .build();
    }
}
