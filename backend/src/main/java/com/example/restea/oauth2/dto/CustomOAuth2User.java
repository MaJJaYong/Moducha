package com.example.restea.oauth2.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final OAuth2JwtMemberDTO oAuth2JwtMemberDTO;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add((GrantedAuthority) oAuth2JwtMemberDTO::getRole);

        return authorities;
    }

    @Override
    public String getName() {
        return oAuth2JwtMemberDTO.getNickname();
    }

    public Integer getUserId() {
        return oAuth2JwtMemberDTO.getUserId();
    }

    public String getNickname() {
        return oAuth2JwtMemberDTO.getNickname();
    }

    public String getRole() {
        return oAuth2JwtMemberDTO.getRole();
    }

    public String getPicture() {
        return oAuth2JwtMemberDTO.getPicture();
    }
}