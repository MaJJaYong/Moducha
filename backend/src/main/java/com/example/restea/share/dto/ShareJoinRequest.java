package com.example.restea.share.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class ShareJoinRequest {
    @NotBlank(message = "name is empty.")
    @Size(max = 30, message = "name is too long.")
    private String name;

    @NotBlank(message = "phone is empty.")
    @Size(max = 13, message = "phone number is too long.") // 010-1234-5678
//    @Size(max = 11, message = "phone number is too long.") // 01012345678
    private String phone;

    @NotBlank(message = "address is empty.")
    @Size(max = 100, message = "address is too long.")
    private String address;
}
