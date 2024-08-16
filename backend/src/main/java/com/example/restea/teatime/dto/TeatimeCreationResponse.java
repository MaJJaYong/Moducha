package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeBoard;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeCreationResponse {
    private final Integer boardId;
    private final String title;
    private final String content;
    private final LocalDateTime createdDate;
    private final LocalDateTime endDate;
    private final LocalDateTime broadcastDate;
    private final Integer maxParticipants;

    public static TeatimeCreationResponse of(TeatimeBoard teatimeBoard) {
        return TeatimeCreationResponse.builder()
                .boardId(teatimeBoard.getId())
                .title(teatimeBoard.getTitle())
                .content(teatimeBoard.getContent())
                .createdDate(teatimeBoard.getCreatedDate())
                .endDate(teatimeBoard.getEndDate())
                .maxParticipants(teatimeBoard.getMaxParticipants())
                .build();
    }
}
