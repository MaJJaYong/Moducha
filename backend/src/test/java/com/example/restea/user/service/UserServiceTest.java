package com.example.restea.user.service;

import static com.example.restea.user.enums.UserMessage.USER_ALREADY_WITHDRAWN;
import static com.example.restea.user.enums.UserMessage.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.restea.oauth2.entity.AuthToken;
import com.example.restea.oauth2.entity.RefreshToken;
import com.example.restea.oauth2.repository.AuthTokenRepository;
import com.example.restea.oauth2.repository.RefreshTokenRepository;
import com.example.restea.record.entity.Record;
import com.example.restea.record.repository.RecordRepository;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareComment;
import com.example.restea.share.entity.ShareParticipant;
import com.example.restea.share.entity.ShareReply;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.share.repository.ShareCommentRepository;
import com.example.restea.share.repository.ShareParticipantRepository;
import com.example.restea.share.repository.ShareReplyRepository;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.entity.TeatimeParticipant;
import com.example.restea.teatime.entity.TeatimeReply;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeCommentRepository;
import com.example.restea.teatime.repository.TeatimeParticipantRepository;
import com.example.restea.teatime.repository.TeatimeReplyRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DisplayName("UserService 통합 테스트")
class UserServiceTest {

    private final UserRepository userRepository;
    private final ShareParticipantRepository shareParticipantRepository;
    private final TeatimeParticipantRepository teatimeParticipantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokenRepository authTokenRepository;
    private final RecordRepository recordRepository;
    private final ShareBoardRepository shareBoardRepository;
    private final ShareCommentRepository shareCommentRepository;
    private final ShareReplyRepository shareReplyRepository;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeCommentRepository teatimeCommentRepository;
    private final TeatimeReplyRepository teatimeReplyRepository;
    private final UserService userService;

    @Autowired
    private EntityManager em;


    @Autowired
    public UserServiceTest(UserRepository userRepository,
                           ShareParticipantRepository shareParticipantRepository,
                           TeatimeParticipantRepository teatimeParticipantRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           AuthTokenRepository authTokenRepository,
                           RecordRepository recordRepository, ShareBoardRepository shareBoardRepository,
                           ShareCommentRepository shareCommentRepository, ShareReplyRepository shareReplyRepository,
                           TeatimeBoardRepository teatimeBoardRepository,
                           TeatimeCommentRepository teatimeCommentRepository,
                           TeatimeReplyRepository teatimeReplyRepository, UserService userService) {
        this.userRepository = userRepository;
        this.shareParticipantRepository = shareParticipantRepository;
        this.teatimeParticipantRepository = teatimeParticipantRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authTokenRepository = authTokenRepository;
        this.recordRepository = recordRepository;
        this.shareBoardRepository = shareBoardRepository;
        this.shareCommentRepository = shareCommentRepository;
        this.shareReplyRepository = shareReplyRepository;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.teatimeCommentRepository = teatimeCommentRepository;
        this.teatimeReplyRepository = teatimeReplyRepository;
        this.userService = userService;
    }

    @Test
    @Transactional
    @DisplayName("유저 회원 탈퇴 시나리오")
    void 회원탈퇴() {
        // given : User와 관련된 연관 엔티티 생성 및 관계 설정
        // AuthToken 생성
        User user = createUser();
        // 기록 생성 - count개 만큼
        createRecord(user);
        // 나눔글, 댓글 ,대댓글 생성
        createShareContents(user);
        // 티타임글, 댓글, 대댓글, 신청 생성
        createTeatimeContents(user);

        // when
        userService.withdrawUser(user.getId());
        em.flush();
        em.clear();

        // then
        // User 상태 확인
        Optional<User> deletedUserOpt = userRepository.findById(user.getId());

        assertTrue(deletedUserOpt.isPresent()); // 사용자가 존재해야함
        User deletedUser = deletedUserOpt.get();
        assertFalse(deletedUser.getActivated()); // 사용자가 비활성화 되었는지 확인

        // Token 제거 확인
        assertNull(deletedUser.getAuthToken());
        assertTrue(authTokenRepository.findAll().isEmpty());

        // RefreshToken Revoke 확인
        assertNull(deletedUser.getRefreshToken());
        assertTrue(refreshTokenRepository.findAll().get(0).getRevoked());

        // Record 제거 확인
        assertTrue(recordRepository.findAll().isEmpty());

        // ShareParticipant 제거 확인
        assertTrue(shareParticipantRepository.findAll().isEmpty());

        // TeatimeParticipant 제거 확인
        assertTrue(teatimeParticipantRepository.findAll().isEmpty());

        // ShareBoard, ShareComment, ShareReply 확인 (남아있어야 함)
        assertEquals(1, shareBoardRepository.findAll().size());
        assertEquals(1, shareCommentRepository.findAll().size());
        assertEquals(1, shareReplyRepository.findAll().size());

        // TeatimeBoard, TeatimeComment, TeatimeReply 확인 (남아있어야 함)
        assertEquals(1, teatimeBoardRepository.findAll().size());
        assertEquals(1, teatimeCommentRepository.findAll().size());
        assertEquals(1, teatimeReplyRepository.findAll().size());
    }

    private User createUser() {
        AuthToken authToken = AuthToken.builder()
                .value("testAuthToken")
                .build();
        authTokenRepository.save(authToken);

        // User 생성, AuthToken 연관관계 설정
        User user = User.builder()
                .nickname("testNickname")
                .authId("google 123123")
                .authToken(authToken)
                .build();

        // RefreshToken 생성, User와 연관관계 설정
        RefreshToken refreshToken = RefreshToken.builder()
                .value("testRefreshToken")
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .build();
        refreshTokenRepository.save(refreshToken);
        user.addRefreshToken(refreshToken);

        userRepository.save(user);
        em.flush();
        em.clear();

        return user;
    }

    private void createRecord(User user) {
        Record record = Record.builder()
                .title("testTitle")
                .content("testContent")
                .user(user)
                .build();

        recordRepository.save(record);
        em.flush();
        em.clear();
    }

    private void createShareContents(User user) {
        ShareBoard shareBoard = ShareBoard.builder()
                .title("testTitle")
                .content("testContent")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .build();
        shareBoardRepository.save(shareBoard);

        ShareComment shareComment = ShareComment.builder()
                .content("testContent")
                .shareBoard(shareBoard)
                .user(user)
                .build();
        shareCommentRepository.save(shareComment);

        ShareReply shareReply = ShareReply.builder()
                .content("testContent")
                .shareComment(shareComment)
                .user(user)
                .build();
        shareReplyRepository.save(shareReply);

        ShareParticipant shareParticipant = ShareParticipant.builder()
                .name("testName")
                .phone("testPhone")
                .address("testAddress")
                .shareBoard(shareBoard)
                .user(user)
                .build();
        shareParticipantRepository.save(shareParticipant);

        em.flush();
        em.clear();
    }

    private void createTeatimeContents(User user) {
        TeatimeBoard teatimeBoard = TeatimeBoard.builder()
                .title("testTitle")
                .content("testContent")
                .broadcastDate(LocalDateTime.now().plusDays(10))
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .build();
        teatimeBoardRepository.save(teatimeBoard);

        TeatimeComment teatimeComment = TeatimeComment.builder()
                .content("testContent")
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build();
        teatimeCommentRepository.save(teatimeComment);

        TeatimeReply teatimeReply = TeatimeReply.builder()
                .content("testContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build();
        teatimeReplyRepository.save(teatimeReply);

        TeatimeParticipant teatimeParticipant = TeatimeParticipant.builder()
                .name("testName")
                .phone("testPhone")
                .address("testAddress")
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build();
        teatimeParticipantRepository.save(teatimeParticipant);

        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    @DisplayName("유저가 존재하지 않을 때 회원 탈퇴 시나리오")
    void 유저가_없을_때_회원탈퇴() {
        // given
        Integer nonExistentUserId = 999; // 존재하지 않는 유저 ID

        // when & then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.withdrawUser(nonExistentUserId);
        });

        // 오류 메시지 확인
        assertEquals(USER_NOT_FOUND.getMessage(), thrown.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("이미 탈퇴한 유저에 대한 회원 탈퇴 시나리오")
    void 이미_탈퇴한_유저_탈퇴_시_에러() {
        // given
        User user = createUser();

        // when
        userService.withdrawUser(user.getId());
        em.flush();
        em.clear();

        // when & then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.withdrawUser(user.getId()); // 이미 탈퇴된 상태이므로 예외 발생해야 함
        });

        // 오류 메시지 확인
        assertEquals(USER_ALREADY_WITHDRAWN.getMessage(), thrown.getMessage());

        // 이후 유저 상태 검토
        Optional<User> deletedUserOpt = userRepository.findById(user.getId());
        assertTrue(deletedUserOpt.isPresent());
        User deletedUser = deletedUserOpt.get();
        assertFalse(deletedUser.getActivated()); // 비활성화 상태 확인
    }
}
