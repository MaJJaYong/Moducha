package com.example.restea.util;

import com.example.restea.oauth2.dto.CustomOAuth2User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class SecurityTestUtil {

  public static void setUpSecurityContext(CustomOAuth2User customOAuth2User) {

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        customOAuth2User, null, customOAuth2User.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
