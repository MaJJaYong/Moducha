package com.example.restea.oauth2.service;

import com.example.restea.oauth2.entity.RefreshToken;
import com.example.restea.oauth2.jwt.JWTUtil;
import com.example.restea.oauth2.repository.RefreshTokenRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Transactional
    public void addRefreshToken(User user, String value) {
        RefreshToken refreshToken = getRefreshToken(value);

        // 연관관계 매핑을 위해 User에 RefreshToken 추가
        if (user.getRefreshToken() != null) { // 원래 RefreshToken이 존재했다면
            revokeExistingRefreshToken(user);
        }

        if (user.getRefreshToken() == null) { // RefreshToken이 없는 상태라면
            refreshToken = createRefreshToken(value);
            user.addRefreshToken(refreshToken);
        }

        // RefreshToken 저장 및 User 저장
        saveRefreshTokenAndUser(user, refreshToken);
    }

    private RefreshToken getRefreshToken(String value) {
        return refreshTokenRepository.findByValue(value);
    }

    private RefreshToken createRefreshToken(String value) {
        LocalDateTime issuedAt = jwtUtil.getIssuedAt(value);
        LocalDateTime expiredAt = jwtUtil.getExpiredAt(value);

        return RefreshToken.builder()
                .value(value)
                .issuedAt(issuedAt)
                .expiredAt(expiredAt)
                .build();
    }


    private void revokeExistingRefreshToken(User user) {
        refreshTokenRepository.revokeById(user.getRefreshToken().getId());
        user.deleteRefreshToken();
    }

    private void saveRefreshTokenAndUser(User user, RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
        userRepository.save(user);
    }
}
