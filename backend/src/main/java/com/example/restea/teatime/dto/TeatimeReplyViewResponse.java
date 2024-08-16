package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeReply;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeReplyViewResponse {
    private final Integer commentId;
    private final Integer replyId;
    private final String content;
    private final LocalDateTime createdDate;
    private final Integer userId;
    private final String nickname;
    private final String picture;

    public static TeatimeReplyViewResponse of(TeatimeReply teatimeReply) {
        return TeatimeReplyViewResponse.builder()
                .commentId(teatimeReply.getTeatimeComment().getId())
                .replyId(teatimeReply.getId())
                .content(teatimeReply.getExposedContent())
                .createdDate(teatimeReply.getCreatedDate())
                .userId(teatimeReply.getUser().getId())
                .nickname(teatimeReply.getExposedNickname())
                .picture(teatimeReply.getUser().getPicture())
                .build();
    }
}
