package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeComment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeCommentCreationResponse {
    private final Integer commentId;
    private final Integer boardId;
    private final String content;
    private final LocalDateTime createdDate;

    public static TeatimeCommentCreationResponse of(TeatimeComment teatimeComment) {
        return TeatimeCommentCreationResponse.builder()
                .commentId(teatimeComment.getId())
                .boardId(teatimeComment.getTeatimeBoard().getId())
                .content(teatimeComment.getContent())
                .createdDate(teatimeComment.getCreatedDate())
                .build();
    }
}
