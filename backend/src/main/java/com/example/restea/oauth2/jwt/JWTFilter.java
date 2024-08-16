package com.example.restea.oauth2.jwt;

import static com.example.restea.oauth2.enums.JWTFilterMessage.EXPIRED_ACCESS_TOKEN;
import static com.example.restea.oauth2.enums.JWTFilterMessage.INVALID_PREFIX;
import static com.example.restea.oauth2.enums.JWTFilterMessage.NO_ACCESS_TOKEN;
import static com.example.restea.oauth2.enums.TokenType.ACCESS;
import static com.example.restea.oauth2.enums.TokenType.BEARER;

import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.dto.OAuth2JwtMemberDTO;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private static final List<Pattern> EXEMPTED_PATTERNS = List.of(
            Pattern.compile("^\\/api/v1/livekit/webhook(?:\\/.*)?$")
    );

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isExemptedUri(request)) { // URI 확인
            doFilter(request, response, filterChain);
            return;
        }

        String header = request.getHeader(ACCESS.getType());
        if (header == null) { // Access Token 존재 확인
            doFilter(request, response, filterChain);
            return;
        }

        if (invalidPrefix(response, header)) { // 접두사 확인
            return;
        }

        String accessToken = extractToken(header);
        if (isTokenInvalid(accessToken, response)) { // 만료되었는지 && 올바른 토큰인지 확인
            return;
        }

        setUpAuthentication(accessToken);
        filterChain.doFilter(request, response);
    }

    // 접두사가 "BEARER "와 일치하는지 확인
    private boolean invalidPrefix(HttpServletResponse response, String header) throws IOException {
        String prefix = extractPrefix(header);

        if (prefix.equals(BEARER.getType())) { // 접두사가 일치한다면 false 반환
            return false;
        }
        // 접두사가 일치하지 않는다면 예외 발생
        setResponse(response, INVALID_PREFIX.toJson());
        return true;
    }

    // 접두사 추출
    private String extractPrefix(String header) {
        return header.substring(0, BEARER.getType().length());
    }

    // 토큰 추출
    private String extractToken(String header) {
        return header.substring(BEARER.getType().length());
    }

    private boolean isExemptedUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        return EXEMPTED_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(requestUri).matches());
    }

    private boolean isTokenInvalid(String accessToken, HttpServletResponse response) throws IOException {
        return isTokenExpired(accessToken, response) || isNotAccessToken(accessToken, response);
    }

    private boolean isTokenExpired(String accessToken, HttpServletResponse response) throws IOException {
        try {
            jwtUtil.isExpired(accessToken);
            // 정상적이라면 Exception이 발생하지 않음
            return false;
        } catch (ExpiredJwtException e) {
            // 만료된 Jwt 토큰이라면
            setResponse(response, EXPIRED_ACCESS_TOKEN.toJson());
            return true;
        }
    }

    private boolean isNotAccessToken(String accessToken, HttpServletResponse response) throws IOException {
        if (jwtUtil.getCategory(accessToken).equals(ACCESS.getType())) {
            return false;
        }
        // AccessToken이 아닐 경우
        setResponse(response, NO_ACCESS_TOKEN.toJson());
        return true;
    }

    private void setResponse(HttpServletResponse response, String message) throws IOException {
        // HttpStatus 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 헤더 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // ResponseBody 설정
        PrintWriter writer = response.getWriter();
        writer.print(message);
        writer.flush();
    }

    private void setUpAuthentication(String accessToken) {
        String nickname = jwtUtil.getNickname(accessToken);
        Integer userId = jwtUtil.getUserId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        OAuth2JwtMemberDTO oAuth2JwtMemberDTO = OAuth2JwtMemberDTO.builder()
                .nickname(nickname)
                .userId(userId)
                .role(role)
                .build();

        //CustomOAuth2User에 유저 정보 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2JwtMemberDTO);
        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null,
                customOAuth2User.getAuthorities());
        // SecurityContextHolder에 일시적인 세션 생성
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
