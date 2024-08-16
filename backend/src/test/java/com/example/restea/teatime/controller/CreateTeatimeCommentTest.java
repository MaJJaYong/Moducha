package com.example.restea.teatime.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.dto.TeatimeCommentCreationRequest;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeCommentRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@ContextConfiguration(classes = ResteaApplication.class)
@AutoConfigureMockMvc
public class CreateTeatimeCommentTest {
    private final WebApplicationContext context;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeCommentRepository teatimeCommentRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public CreateTeatimeCommentTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                    WebApplicationContext context,
                                    TeatimeBoardRepository teatimeBoardRepository,
                                    TeatimeCommentRepository teatimeCommentRepository, UserRepository userRepository,
                                    CustomOAuth2UserService custumOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.teatimeCommentRepository = teatimeCommentRepository;
        this.userRepository = userRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
    }

    /**
     * testName, content
     */
    private static Stream<Arguments> validParameter() {
        return Stream.of(
                Arguments.of("[한글] 댓글 등록 테스트", "안녕하세요."),
                Arguments.of("[영어] 댓글 등록 테스트", "Hello."),
                Arguments.of("[숫자] 댓글 등록 테스트", "123"),
                Arguments.of("[한글, 영어, 숫자] 댓글 등록 테스트", "abc가나다123")
        );
    }

    /**
     * testName, content
     */
    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("내용이 null인 경우", null),

                Arguments.of("내용이 빈 경우", ""),

                Arguments.of("내용이 100 초과인 경우", "저".repeat(101))
        );
    }

    @Transactional
    @BeforeEach
    void setUp() {
        teatimeCommentRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .alwaysDo(print())
                .build();
    }

    @BeforeEach
    public void OAuth2UserSetup() {
        customOAuth2User = custumOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        teatimeCommentRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("validParameter")
    @DisplayName("[Created] createTeatimeComment : 티타임 게시글 댓글 작성")
    void createTeatimeComment_Success(String testName, String content) throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(4)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";
        final TeatimeCommentCreationRequest teatimeCommentCreationRequest = new TeatimeCommentCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(teatimeCommentCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());
        List<TeatimeComment> teatimeComments = teatimeCommentRepository.findAll();
        assertThat(teatimeComments.size()).isEqualTo(1);
        assertThat(teatimeComments.get(0).getContent()).isEqualTo(content);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidParameter")
    @DisplayName("[BadRequest] createTeatimeComment : 티타임 게시글 댓글 작성")
    void createTeatimeComment_Failure(String testName, String content) throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(4)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";
        final TeatimeCommentCreationRequest teatimeCommentCreationRequest = new TeatimeCommentCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(teatimeCommentCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<TeatimeComment> teatimeComments = teatimeCommentRepository.findAll();
        assertThat(teatimeComments.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeComment : teatimeBoard가 비활성화됐을 때 실패한다.")
    @Test
    public void createTeatimeComment_deactivatedCommentBoard_Failure() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(4)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeBoard testTeatimeBoard = teatimeBoardRepository.findById(teatimeBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 티타임 게시판 생성 실패"));

        testTeatimeBoard.deactivate();

        teatimeBoardRepository.save(testTeatimeBoard);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";
        final String content = "TestContent";
        final TeatimeCommentCreationRequest teatimeCommentCreationRequest = new TeatimeCommentCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(teatimeCommentCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<TeatimeComment> teatimeComments = teatimeCommentRepository.findAll();
        assertThat(teatimeComments.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeComment : teatimeBoard 작성자가 비활성화됐을 때 실패한다.")
    @Test
    public void createTeatimeComment_deactivatedTeatimeBoardUser_Failure() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .authId("authId2")
                .nickname("TestUser2")
                .build());

        User testUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 회원2 생성 실패"));

        testUser.deactivate();
        userRepository.save(testUser);

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(4)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(testUser)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";
        final String content = "TestContent";
        final TeatimeCommentCreationRequest teatimeCommentCreationRequest = new TeatimeCommentCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(teatimeCommentCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<TeatimeComment> teatimeComments = teatimeCommentRepository.findAll();
        assertThat(teatimeComments.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeComment : teatimeBoard가 없을 때 실패한다.")
    @Test
    public void createTeatimeComment_NotFoundTeatimeBoard_Failure() throws Exception {
        // given
        final String url = "/api/v1/teatimes/999/comments";
        final String content = "TestContent";
        final TeatimeCommentCreationRequest teatimeCommentCreationRequest = new TeatimeCommentCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(teatimeCommentCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<TeatimeComment> teatimeComments = teatimeCommentRepository.findAll();
        assertThat(teatimeComments.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeComment : 비활성화된 회원이 댓글 작성을 요청할 때 실패한다.")
    @Test
    public void createTeatimeComment_deactivatedUser_Failure() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        user.deactivate();
        userRepository.save(user);

        User user2 = userRepository.save(User.builder()
                .authId("authId2")
                .nickname("TestUser2")
                .build());

        User testUser = userRepository.findById(user2.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 회원2 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(4)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(testUser)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";
        final String content = "TestContent";
        final TeatimeCommentCreationRequest teatimeCommentCreationRequest = new TeatimeCommentCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(teatimeCommentCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isUnauthorized());
        List<TeatimeComment> teatimeComments = teatimeCommentRepository.findAll();
        assertThat(teatimeComments.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeComment : 등록되지 않은 회원이 댓글 작성을 요청할 때 실패한다.")
    @Test
    public void createTeatimeComment_NotFoundUser_Failure() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        userRepository.delete(user);

        User user2 = userRepository.save(User.builder()
                .authId("authId2")
                .nickname("TestUser2")
                .build());

        User testUser = userRepository.findById(user2.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 회원2 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(4)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(testUser)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";
        final String content = "TestContent";
        final TeatimeCommentCreationRequest teatimeCommentCreationRequest = new TeatimeCommentCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(teatimeCommentCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<TeatimeComment> teatimeComments = teatimeCommentRepository.findAll();
        assertThat(teatimeComments.size()).isEqualTo(0);
    }
}
