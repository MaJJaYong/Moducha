package com.example.restea.oauth2.service;

import static com.example.restea.oauth2.enums.ReissueStatus.REFRESH_EXPIRED;
import static com.example.restea.oauth2.enums.ReissueStatus.REFRESH_INVALID;
import static com.example.restea.oauth2.enums.ReissueStatus.REFRESH_NULL;
import static com.example.restea.oauth2.enums.TokenType.BEARER;
import static com.example.restea.oauth2.enums.TokenType.REFRESH;

import com.example.restea.oauth2.jwt.JWTUtil;
import com.example.restea.oauth2.repository.RefreshTokenRepository;
import com.example.restea.oauth2.util.CookieMethods;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieMethods cookieMethods;

    public void validateToken(String refreshToken, HttpServletResponse response) {
        // 토큰 존재 여부 확인
        if (isTokenNull(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REFRESH_NULL.getMessage());
        }

        // 토큰 만료 여부 확인
        if (isTokenExpired(refreshToken)) {
            refreshTokenRepository.revokeByValue(refreshToken); // Refresh Token revoke 처리
            cookieMethods.clearRefreshTokenCookie(response); // 쿠키에서 Refresh Token 삭제
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REFRESH_EXPIRED.getMessage());
        }

        // 토큰 카테고리 확인
        if (isTokenInvalid(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REFRESH_INVALID.getMessage());
        }
    }

    // 토큰 존재 확인
    private boolean isTokenNull(String refreshToken) {
        return refreshToken == null;
    }

    // 토큰 만료 확인
    private boolean isTokenExpired(String refreshToken) {
        try {
            jwtUtil.isExpired(refreshToken);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // 토큰 카테고리 확인
    private boolean isTokenInvalid(String refreshToken) {
        String category = jwtUtil.getCategory(refreshToken);
        return !category.equals(REFRESH.getType());
    }

    // 새로운 AccessToken 발급
    public String createNewAccessToken(String refresh) {
        String nickname = jwtUtil.getNickname(refresh);
        Integer userId = jwtUtil.getUserId(refresh);
        String picture = jwtUtil.getPicture(refresh);
        String role = jwtUtil.getRole(refresh);

        return BEARER.getType() + jwtUtil.createAccessToken(userId, nickname, picture, role);
    }

    // RefreshToken이 존재하는지 확인
    public void checkRefresh(String refreshToken) {
        boolean isExist = refreshTokenRepository.existsByValue(refreshToken);

        if (!isExist) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, REFRESH_INVALID.getMessage());
        }
    }
}