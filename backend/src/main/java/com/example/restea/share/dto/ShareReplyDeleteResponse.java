package com.example.restea.share.dto;

import com.example.restea.share.entity.ShareReply;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareReplyDeleteResponse {

    private final Integer replyId;

    public static ShareReplyDeleteResponse from(ShareReply reply) {
        return ShareReplyDeleteResponse.builder()
                .replyId(reply.getId())
                .build();
    }

}
