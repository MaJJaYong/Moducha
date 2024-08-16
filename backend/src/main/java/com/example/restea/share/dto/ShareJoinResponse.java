package com.example.restea.share.dto;

import com.example.restea.share.entity.ShareParticipant;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShareJoinResponse {
    private Integer participantId;
    private LocalDateTime createdDate;
    private String name;
    private String phone;
    private String address;
    private Integer userId;
    private Integer boardId;

    public static ShareJoinResponse of(@NotNull ShareParticipant shareParticipant) {
        return ShareJoinResponse.builder()
                .participantId(shareParticipant.getId())
                .createdDate(shareParticipant.getCreatedDate())
                .name(shareParticipant.getName())
                .phone(shareParticipant.getPhone())
                .address(shareParticipant.getAddress())
                .userId(shareParticipant.getUser().getId())
                .boardId(shareParticipant.getShareBoard().getId())
                .build();
    }
}