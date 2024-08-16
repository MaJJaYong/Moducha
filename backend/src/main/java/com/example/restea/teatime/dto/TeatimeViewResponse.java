package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeBoard;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeViewResponse {
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
    private final String nickname;
    private final Integer userId;
    private final String picture;

    public static TeatimeViewResponse of(TeatimeBoard teatimeBoard, Integer participants, String picture) {
        return TeatimeViewResponse.builder()
                .boardId(teatimeBoard.getId())
                .title(teatimeBoard.getTitle())
                .content(teatimeBoard.getContent())
                .createdDate(teatimeBoard.getCreatedDate())
                .lastUpdated(teatimeBoard.getLastUpdated())
                .endDate(teatimeBoard.getEndDate())
                .broadcastDate(teatimeBoard.getBroadcastDate())
                .maxParticipants(teatimeBoard.getMaxParticipants())
                .participants(participants)
                .viewCount(teatimeBoard.getViewCount())
                .nickname(teatimeBoard.getUser().getExposedNickname())
                .userId(teatimeBoard.getUser().getId())
                .picture(picture)
                .build();
    }
}
