package com.example.restea.oauth2.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2JwtMemberDTO {

    private Integer userId;
    private String nickname;
    private String picture;
    private String role;
}