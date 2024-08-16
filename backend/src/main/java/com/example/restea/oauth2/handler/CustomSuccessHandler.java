package com.example.restea.oauth2.handler;


import static com.example.restea.oauth2.enums.TokenType.REFRESH;
import static com.example.restea.user.enums.UserMessage.USER_NOT_FOUND;

import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.jwt.JWTUtil;
import com.example.restea.oauth2.service.RefreshTokenService;
import com.example.restea.oauth2.util.CookieMethods;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final CookieMethods cookieMethods;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Value("${app.redirect.uri}")
    private String appRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String nickname = customUserDetails.getNickname();
        Integer userId = customUserDetails.getUserId();
        String picture = customUserDetails.getPicture();
        String role = extractUserRole(authentication);

        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(userId, nickname, picture, role);
        String refreshToken = jwtUtil.createRefreshToken(userId, nickname, picture, role);

        // Refresh 토큰 저장
        saveRefreshToken(userId, refreshToken);
        addRefreshTokenToResponse(response, refreshToken);

        // Redirect
        redirectToTargetWithToken(request, response, accessToken);
    }

    private String extractUserRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.iterator().next().getAuthority();
    }

    private void saveRefreshToken(Integer userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND.getMessage()));

        refreshTokenService.addRefreshToken(user, refreshToken);
    }

    private void addRefreshTokenToResponse(HttpServletResponse response, String refreshToken) {
        Cookie refreshCookie = cookieMethods.createCookie(REFRESH.getType(), refreshToken);
        response.addCookie(refreshCookie);
    }

    private void redirectToTargetWithToken(HttpServletRequest request, HttpServletResponse response, String accessToken)
            throws IOException {
        response.setStatus(HttpStatus.OK.value());
        String targetUrl = getTargetUrl(accessToken);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // 액세스 토큰을 queryParam에 추가
    private String getTargetUrl(String accessToken) {
        return UriComponentsBuilder.fromUriString(appRedirectUri)
                .queryParam("access", accessToken)
                .build()
                .toUriString();
    }
}
