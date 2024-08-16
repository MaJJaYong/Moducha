package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeBoard;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeListResponse {

    private final Integer boardId;
    private final String title;
    private final String content;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdated;
    private final LocalDateTime broadcastDate;
    private final LocalDateTime endDate;
    private final Integer maxParticipants;
    private final Integer participants;
    private final Integer viewCount;
    private final String nickname;

    public static TeatimeListResponse of(TeatimeBoard teatimeBoard, Integer participants) {
        return TeatimeListResponse.builder()
                .boardId(teatimeBoard.getId())
                .title(teatimeBoard.getTitle())
                .content(teatimeBoard.getContent())
                .createdDate(teatimeBoard.getCreatedDate())
                .lastUpdated(teatimeBoard.getLastUpdated())
                .broadcastDate(teatimeBoard.getBroadcastDate())
                .endDate(teatimeBoard.getEndDate())
                .maxParticipants(teatimeBoard.getMaxParticipants())
                .participants(participants)
                .viewCount(teatimeBoard.getViewCount())
                .nickname(teatimeBoard.getUser().getExposedNickname())
                .build();
    }
}
