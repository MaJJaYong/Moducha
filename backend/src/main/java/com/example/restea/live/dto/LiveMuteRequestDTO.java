package com.example.restea.live.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class LiveMuteRequestDTO {
    @NotNull(message = "empty userId.")
    private Integer userId;

    @NotBlank(message = "empty trackSid.")
    private String trackSid;

    @NotNull(message = "empty isMute.")
    private Boolean isMute;
}
