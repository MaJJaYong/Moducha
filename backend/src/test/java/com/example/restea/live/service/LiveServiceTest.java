package com.example.restea.live.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.example.restea.live.entity.Live;
import com.example.restea.live.repository.LiveRepository;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeParticipant;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeParticipantRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import io.livekit.server.AccessToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("Live Service")
@ExtendWith(MockitoExtension.class)
class LiveServiceTest {

    // 테스트 주체의 외부 의존성들 -> Mock 객체로 생성하기
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeatimeBoardRepository teatimeBoardRepository;
    @Mock
    private TeatimeParticipantRepository teatimeParticipantRepository;
    @Mock
    private LiveRepository liveRepository;

    // 단위 테스트의 주체 -> @InjectMocks로 가짜 의존성 주입받기
    @InjectMocks
    private LiveService liveService;

    private User testUser;
    private User testUser2;
    private TeatimeBoard testTeatimeBoard;
    private TeatimeBoard testTeatimeBoard2;
    private TeatimeParticipant testTeatimeParticipant;


    @BeforeEach
    void setUp() {
        testUser = createUser(1);
        testUser2 = createUser(2);

        testTeatimeBoard = createTeatimeBoard(testUser, true, 1);
        testTeatimeBoard2 = createTeatimeBoard(testUser, false, 2);

        testTeatimeParticipant = createTeatimeParticipant(testTeatimeBoard, testUser2);

        ReflectionTestUtils.setField(liveService, "LIVEKIT_API_KEY", "aaa");
        ReflectionTestUtils.setField(liveService, "LIVEKIT_API_SECRET", "bbb");
    }

    private User createUser(int id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "activated", true);
        return user;
    }

    private TeatimeBoard createTeatimeBoard(User user, boolean activated, int id) {
        TeatimeBoard board = TeatimeBoard.builder()
                .broadcastDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .user(user)
                .build();
        ReflectionTestUtils.setField(board, "id", id);
        ReflectionTestUtils.setField(board, "activated", activated);
        return board;
    }

    private TeatimeParticipant createTeatimeParticipant(TeatimeBoard teatimeBoard, User user) {
        return TeatimeParticipant.builder()
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build();
    }


    @ParameterizedTest(name = "Live : {0}")
    @CsvSource({
            "false",
            "true"
    })
    @DisplayName("방송 생성 여부 조회 테스트 - 성공")
    void isLiveOpenSuccess(boolean liveExists) throws Exception {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(1, true)).thenReturn(Optional.of(testTeatimeBoard));
        when(liveRepository.existsByTeatimeBoard(testTeatimeBoard)).thenReturn(liveExists);

        // When
        boolean isOpen = liveService.isLiveOpen(1, 1);

        // Then
        Assertions.assertThat(isOpen).isEqualTo(liveExists);
    }

    @Test
    @DisplayName("방송 생성 여부 조회 테스트 - 실패 : 티타임 게시글이 없는 경우")
    void isLiveOpenFailTeatimeBoardNotFound() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.isLiveOpen(10, 1));

        assertEquals("404 NOT_FOUND \"TeatimeBoard not found.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 생성 여부 조회 테스트 - 실패 : 티타임 게시글이 삭제된 경우")
    void isLiveOpenFailTeatimeBoardNotActivated() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard2));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.isLiveOpen(10, 1));

        assertEquals("400 BAD_REQUEST \"TeatimeBoard not activated.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 생성 여부 조회 테스트 - 실패 : 티타임 게시글이 참가자가 아닌 경우")
    void isLiveOpenFailNotTeatimeBoardParticipant() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser2));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard));
        when(teatimeParticipantRepository.existsByTeatimeBoardAndUser(testTeatimeBoard, testUser2))
                .thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.isLiveOpen(10, 2));

        assertEquals("403 FORBIDDEN \"Not a participant.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 생성 테스트 - 성공")
    void createLiveSuccess() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard));
        when(liveRepository.save(any(Live.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AccessToken token = liveService.createLive(testTeatimeBoard.getId(), 1);

        // Then
        assertNotNull(token);
        assertEquals(testUser.getId().toString(), token.getIdentity());
        assertEquals(testUser.getNickname(), token.getName());
    }


    @Test
    @DisplayName("방송 생성 테스트 - 실패 : 티타임 게시글이 없는 경우")
    void createLiveFailTeatimeBoardNotFound() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.createLive(10, 1));

        assertEquals("404 NOT_FOUND \"TeatimeBoard not found.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 생성 테스트 - 실패 : 티타임 게시글이 삭제된 경우")
    void createLiveFailTeatimeBoardNotActivated() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard2));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.createLive(10, 1));

        assertEquals("400 BAD_REQUEST \"TeatimeBoard not activated.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 생성 여부 조회 테스트 - 실패 : 티타임 게시글이 작성자가 아닌 경우")
    void createLiveFailNotTeatimeBoardWriter() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser2));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard));

        // When $ Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.createLive(1, 2));

        assertEquals("403 FORBIDDEN \"Not a writer.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 생성 여부 조회 테스트 - 실패 : 방송 예정일보다 31분전인 경우")
    void createLiveFailBeforeBroadcastDate() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        ReflectionTestUtils.setField(testTeatimeBoard, "broadcastDate", LocalDateTime.now().plusMinutes(31));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.createLive(1, 1));

        assertEquals("403 FORBIDDEN \"Before 30minutes the broadcast date.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 생성 여부 조회 테스트 - 실패 : 방송 예정일보다 31분후인 경우")
    void createLiveFailAfterBroadcastDate() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        ReflectionTestUtils.setField(testTeatimeBoard, "broadcastDate", LocalDateTime.now().minusMinutes(31));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.createLive(1, 1));

        assertEquals("403 FORBIDDEN \"After 30minutes the broadcast date.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 생성 여부 조회 테스트 - 실패 : 방송 예정일과 다른 날인 경우")
    void createLiveFailDifferentBroadcastDate() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        ReflectionTestUtils.setField(testTeatimeBoard, "broadcastDate", LocalDateTime.now().plusDays(5));
        when(teatimeBoardRepository.findByIdAndActivated(1, true)).thenReturn(Optional.of(testTeatimeBoard));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.createLive(1, 1));

        assertEquals("403 FORBIDDEN \"Different from the broadcast date.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 참가 테스트 - 성공")
    void liveJoinSuccess() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        Live live = Live.builder()
                .teatimeBoard(testTeatimeBoard)
                .build();
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard));
        when(liveRepository.findByTeatimeBoard(any(TeatimeBoard.class))).thenReturn(Optional.of(live));

        // When

        AccessToken token = liveService.liveJoin(testTeatimeBoard.getId(), 1);

        // Then
        assertNotNull(token);
        assertEquals(testUser.getId().toString(), token.getIdentity());
        assertEquals(testUser.getNickname(), token.getName());
    }

    @Test
    @DisplayName("방송 참가 테스트 - 실패 : 티타임 게시글이 없는 경우")
    void liveJoinFailTeatimeBoardNotFound() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.liveJoin(10, 1));

        assertEquals("404 NOT_FOUND \"TeatimeBoard not found.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 참가 테스트 - 실패 : 티타임 게시글이 삭제된 경우")
    void liveJoinFailTeatimeBoardNotActivated() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard2));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.liveJoin(10, 1));

        assertEquals("400 BAD_REQUEST \"TeatimeBoard not activated.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 참가 테스트 - 실패 : 티타임 게시글이 참가자가 아닌 경우")
    void liveJoinFailNotTeatimeBoardParticipant() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser2));
        when(teatimeBoardRepository.findByIdAndActivated(anyInt(), anyBoolean())).thenReturn(
                Optional.of(testTeatimeBoard));
        when(teatimeParticipantRepository.existsByTeatimeBoardAndUser(testTeatimeBoard, testUser2))
                .thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.liveJoin(10, 2));

        assertEquals("403 FORBIDDEN \"Not a participant.\"", exception.getMessage());
    }

    @Test
    @DisplayName("방송 참가 테스트 - 실패 : 방송이 존재하지 않는 경우")
    void liveJoinFailLiveNotFound() {

        // Given
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(teatimeBoardRepository.findByIdAndActivated(1, true)).thenReturn(Optional.of(testTeatimeBoard));
        when(liveRepository.findByTeatimeBoard(testTeatimeBoard)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                liveService.liveJoin(1, 2));

        assertEquals("404 NOT_FOUND \"Live not found.\"", exception.getMessage());
    }


    @Test
    void createToken() {
    }

    @Test
    void webHook() {
    }
}