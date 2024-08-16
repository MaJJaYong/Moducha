package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeBoard;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeUpdateResponse {

    private final Integer boardId;
    private final String title;
    private final String content;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdated;
    private final LocalDateTime endDate;
    private final LocalDateTime broadcastDate;
    private final Integer maxParticipants;
    private final Integer participants;
    private final Integer viewCount;

    public static TeatimeUpdateResponse of(TeatimeBoard teatimeBoard, Integer participants) {
        return TeatimeUpdateResponse.builder()
                .boardId(teatimeBoard.getId())
                .title(teatimeBoard.getTitle())
                .content(teatimeBoard.getContent())
                .createdDate(teatimeBoard.getCreatedDate())
                .lastUpdated(teatimeBoard.getLastUpdated())
                .endDate(teatimeBoard.getEndDate())
                .endDate(teatimeBoard.getBroadcastDate())
                .maxParticipants(teatimeBoard.getMaxParticipants())
                .participants(participants)
                .viewCount(teatimeBoard.getViewCount())
                .build();
    }
}
