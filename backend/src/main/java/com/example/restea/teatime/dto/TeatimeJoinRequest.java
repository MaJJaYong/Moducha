package com.example.restea.teatime.dto;

import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeParticipant;
import com.example.restea.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class TeatimeJoinRequest {
    @NotBlank(message = "empty name.")
    private String name;

    @NotBlank(message = "empty phone.")
    private String phone;

    @NotBlank(message = "empty address.")
    private String address;

    public TeatimeParticipant toEntity(TeatimeBoard teatimeBoard, User user) {
        return TeatimeParticipant.builder()
                .name(name)
                .phone(phone)
                .address(address)
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build();
    }
}
