package com.example.restea.oauth2.jwt;

import static com.example.restea.oauth2.enums.TokenType.ACCESS;
import static com.example.restea.oauth2.enums.TokenType.REFRESH;

import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    private static final Long MS_TO_S = 1000L;
    private final SecretKey secretKey;

    /**
     * application.properties에서 저장해 준 암호화 키를 이용해 secretKey 객체 생성
     */
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // nickname 얻기
    public String getNickname(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("nickname", String.class);
    }

    // userId 얻기
    public Integer getUserId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("userId", Integer.class);
    }

    // role 얻기
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("role", String.class);
    }

    // Category 값 얻기
    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("category", String.class);
    }

    // Picture 값 얻기
    public String getPicture(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("picture", String.class);
    }

    // IssuedAt 값 얻기
    public LocalDateTime getIssuedAt(String token) {
        return LocalDateTime.ofInstant(Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .getIssuedAt().toInstant(), ZoneId.systemDefault());
    }

    // ExpiredAt 값 얻기
    public LocalDateTime getExpiredAt(String token) {
        return LocalDateTime.ofInstant(Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .getExpiration().toInstant(), ZoneId.systemDefault());
    }

    // 만료 되었는지?
    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .getExpiration()
                .before(new Date()); // 현재 Date가 Expiration 시점 이전인가?
    }

    public String createAccessToken(Integer userId, String nickname, String picture, String role) {
        return createJwt(ACCESS.getType(), userId, nickname, picture, role, ACCESS.getExpiration() * MS_TO_S);
    }

    public String createRefreshToken(Integer userId, String nickname, String picture, String role) {
        return createJwt(REFRESH.getType(), userId, nickname, picture, role, REFRESH.getExpiration() * MS_TO_S);
    }

    // 토큰 발급
    private String createJwt(String category, Integer userId, String nickname, String picture, String role,
                             Long expiredMs) {
        return Jwts.builder()
                .claim("category", category) // access, refresh 판단
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("picture", picture)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}