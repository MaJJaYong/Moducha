package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeReply;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeReplyDeleteResponse {
    private final Integer replyId;

    public static TeatimeReplyDeleteResponse from(TeatimeReply reply) {
        return TeatimeReplyDeleteResponse.builder()
                .replyId(reply.getId())
                .build();
    }
}
