package com.example.restea.user.service;

import static com.example.restea.oauth2.enums.TokenType.BEARER;
import static com.example.restea.oauth2.enums.TokenType.REFRESH;
import static com.example.restea.user.enums.UserMessage.USER_ALREADY_WITHDRAWN;
import static com.example.restea.user.enums.UserMessage.USER_INVALID;
import static com.example.restea.user.enums.UserMessage.USER_NICKNAME_EXISTS;
import static com.example.restea.user.enums.UserMessage.USER_NICKNAME_SAME;
import static com.example.restea.user.enums.UserMessage.USER_NOT_FOUND;

import com.example.restea.oauth2.jwt.JWTUtil;
import com.example.restea.oauth2.repository.AuthTokenRepository;
import com.example.restea.oauth2.repository.RefreshTokenRepository;
import com.example.restea.oauth2.service.RefreshTokenService;
import com.example.restea.oauth2.util.CookieMethods;
import com.example.restea.record.repository.RecordRepository;
import com.example.restea.share.repository.ShareParticipantRepository;
import com.example.restea.teatime.repository.TeatimeParticipantRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import jakarta.persistence.LockModeType;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String GOOGLE_REVOKE_URL = "https://accounts.google.com/o/oauth2/revoke?token=";

    private final UserRepository userRepository;
    private final ShareParticipantRepository shareParticipantRepository;
    private final TeatimeParticipantRepository teatimeParticipantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokenRepository authTokenRepository;
    private final RecordRepository recordRepository;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CookieMethods cookieMethods;
    private final RestTemplate restTemplate;

    @Transactional
    public String withdrawUser(Integer userId) {
        User user = findActiveUser(userId);
        String authToken = user.getAuthToken().getValue();
        performUserDeactivation(user); // 삭제, 비활성화, revoke

        return authToken;
    }

    private @NotNull User findActiveUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND.getMessage()));

        // 이미 탈퇴한 유저의 경우 다시 탈퇴 할 수 없음.
        if (!user.getActivated()) {
            throw new IllegalArgumentException(USER_ALREADY_WITHDRAWN.getMessage());
        }
        return user;
    }

    private void performUserDeactivation(User user) {
        user.changeDeactivatedPicture(); // 프로필 사진 삭제
        deleteRecords(user); // 기록 삭제
        deleteParticipants(user); // 참여기록 clear 및 삭제
        revokeRefreshToken(user); // RefreshToken를 지운 후 Revoke 처리
        deleteAuthToken(user); // AuthToken을 지운 후 삭제
        user.deactivate(); // 유저 비활성화
    }

    // 구글 OAuth2 해제
    public void revokeGoogleAuth(String value) {
        String googleRevokeUrl = GOOGLE_REVOKE_URL + value;
        restTemplate.getForObject(googleRevokeUrl, String.class);
    }

    /**
     * 기록 삭제 메소드
     *
     * @param user 유저 엔티티
     */
    private void deleteRecords(User user) {
        user.clearRecords();
        recordRepository.deleteByUserId(user.getId());
    }

    /**
     * 참여기록 삭제 메소드
     *
     * @param user 유저 엔티티
     */
    private void deleteParticipants(User user) {
        user.clearParticipants();
        shareParticipantRepository.deleteByUserId(user.getId());
        teatimeParticipantRepository.deleteByUserId(user.getId());
    }

    /**
     * 유저의 RefreshToken을 지운 후 Revoke 상태 활성화
     *
     * @param user 유저 엔티티
     */
    private void revokeRefreshToken(User user) {
        if (user.getRefreshToken() != null) {
            Integer refreshTokenId = user.getRefreshToken().getId();
            user.deleteRefreshToken();
            refreshTokenRepository.revokeById(refreshTokenId);
        }
    }

    /**
     * 유저의 AuthToken을 지운 후 삭제
     *
     * @param user 유저 엔티티
     */
    private void deleteAuthToken(User user) {
        if (user.getAuthToken() != null) {
            Integer authTokenId = user.getAuthToken().getId();
            user.deleteAuthToken();
            authTokenRepository.deleteById(authTokenId);
        }
    }

    /**
     * 올바른 유저인지 확인하는 메소드
     *
     * @param userId       userId
     * @param targetUserId 토큰에서 얻은 userId
     * @return User 객체
     */
    public User checkValidUser(Integer userId, Integer targetUserId) {
        if (!userId.equals(targetUserId)) { // 유저의 user_id가 일치하지 않으면
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_INVALID.getMessage());
        }

        User user = userRepository.findById(userId) // 해당 userId를 가진 User가 없으면
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND.getMessage()));

        if (!user.getActivated()) { // 탈퇴된 유저라면
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_ALREADY_WITHDRAWN.getMessage());
        }

        return user;
    }

    /**
     * @param user     checkValidUser에서 검증된 유저
     * @param nickname 변경할 닉네임
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void changeNickname(User user, String nickname) {
        if (user.getNickname().equals(nickname)) { // 바꿀 닉네임이 현재 닉네임과 동일한 경우
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NICKNAME_SAME.getMessage());
        }

        if (checkExistingNickname(nickname)) { // 바꿀 닉네임이 이미 선점된 경우
            throw new ResponseStatusException(HttpStatus.CONFLICT, USER_NICKNAME_EXISTS.getMessage());
        }

        changeUserNickname(user, nickname);
    }

    /**
     * @param nickname 바꿀 닉네임
     * @return 닉네임이 선점되었는지
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    private boolean checkExistingNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 닉네임을 변경한다.
     *
     * @param user     사용자
     * @param nickname 바꿀 닉네임
     */
    private void changeUserNickname(User user, String nickname) {
        user.changeNickname(nickname);
        userRepository.save(user);
    }

    /**
     * AccessToken을 반환하는 메소드
     *
     * @param user 사용자
     * @return AccessToken
     */
    public String getAccessToken(User user) {
        Integer userId = user.getId();
        String nickname = user.getNickname();
        String picture = user.getPicture();
        String role = user.getRole().name();

        return BEARER.getType() + jwtUtil.createAccessToken(userId, nickname, picture, role);
    }

    /**
     * Cookie형태의 RefreshToken을 반환하는 메소드
     *
     * @param user 사용자
     * @return RefreshToken
     */
    public Cookie getRefreshToken(User user) {
        Integer userId = user.getId();
        String nickname = user.getNickname();
        String picture = user.getPicture();
        String role = user.getRole().name();

        String refreshToken = jwtUtil.createRefreshToken(userId, nickname, picture, role);
        refreshTokenService.addRefreshToken(user, refreshToken);

        return cookieMethods.createCookie(REFRESH.getType(), refreshToken);
    }
}
