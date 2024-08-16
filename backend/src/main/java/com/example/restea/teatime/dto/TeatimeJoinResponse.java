package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeParticipant;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeatimeJoinResponse {
    private final Integer participantId;
    private final LocalDateTime createdDate;
    private final String name;
    private final String phone;
    private final String address;
    private final Integer userId;
    private final Integer boardId;

    public static TeatimeJoinResponse of(TeatimeParticipant teatimeParticipant) {
        return TeatimeJoinResponse.builder()
                .participantId(teatimeParticipant.getId())
                .createdDate(teatimeParticipant.getCreatedDate())
                .name(teatimeParticipant.getName())
                .phone(teatimeParticipant.getPhone())
                .address(teatimeParticipant.getAddress())
                .userId(teatimeParticipant.getUser().getId())
                .boardId(teatimeParticipant.getTeatimeBoard().getId())
                .build();
    }
}
