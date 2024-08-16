package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeReply;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeReplyCreationResponse {
    private final Integer commentId;
    private final Integer replyId;
    private final String content;
    private final String nickname;

    public static TeatimeReplyCreationResponse of(TeatimeReply teatimeReply, String nickname) {
        return TeatimeReplyCreationResponse.builder()
                .commentId(teatimeReply.getTeatimeComment().getId())
                .replyId(teatimeReply.getId())
                .content(teatimeReply.getContent())
                .nickname(nickname)
                .build();
    }
}
