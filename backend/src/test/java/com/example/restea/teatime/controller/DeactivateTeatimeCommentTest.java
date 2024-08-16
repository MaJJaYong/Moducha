package com.example.restea.teatime.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeCommentRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class DeactivateTeatimeCommentTest {
    private final WebApplicationContext context;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final TeatimeCommentRepository teatimeCommentRepository;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public DeactivateTeatimeCommentTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
                                        TeatimeBoardRepository teatimeBoardRepository, UserRepository userRepository,
                                        CustomOAuth2UserService customOAuth2UserService,
                                        TeatimeCommentRepository teatimeCommentRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.userRepository = userRepository;
        this.customOAuth2UserService = customOAuth2UserService;
        this.teatimeCommentRepository = teatimeCommentRepository;
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
        customOAuth2User = customOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        teatimeCommentRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("deactivateTeatimeComment : 티타임 게시글 댓글 비활성화에 성공한다.")
    @Test
    public void deactivateTeatimeComment_Success() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeComment teatimeComment = teatimeCommentRepository.save(TeatimeComment.builder()
                .content("TestContent")
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build());

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/deactivated-comments/" + teatimeComment.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isOk());
        TeatimeComment deactivatedTeatimeComment = teatimeCommentRepository.findById(teatimeComment.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 댓글 생성 실패"));
        assertThat(deactivatedTeatimeComment.getActivated()).isFalse();
    }

    @DisplayName("deactivateTeatimeComment 실패 - 존재하지 않는 댓글")
    @Test
    public void deactivateTeatimeComment_NotFound_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        String url = "/api/v1/shares/" + teatimeBoard.getId() + "/deactivated-comments/999";

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("deactivateTeatimeComment 실패 - 이미 비활성화된 댓글")
    @Test
    public void deactivateTeatimeComment_AlreadyDeactivatedTeatimeComment_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeComment teatimeComment = teatimeCommentRepository.save(TeatimeComment.builder()
                .content("TestContent")
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build());

        TeatimeComment deactivatedTeatimeComment = teatimeCommentRepository.findById(teatimeComment.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 댓글 생성 실패"));

        deactivatedTeatimeComment.deactivate();
        teatimeCommentRepository.save(deactivatedTeatimeComment);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/deactivated-comments/" + teatimeComment.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("deactivateTeatimeComment 실패 - 권한이 없는 사용자")
    @Test
    public void deactivateTeatimeComment_Forbidden_Fail() throws Exception {

        // given
        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeComment teatimeComment = teatimeCommentRepository.save(TeatimeComment.builder()
                .content("TestContent")
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build());

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/deactivated-comments/" + teatimeComment.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("deactivateTeatimeComment 실패 - teatimeBoard에 댓글이 아닌 경우")
    @Test
    public void deactivateTeatimeComment_NotFoundComment_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeBoard teatimeBoard2 = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle2")
                .content("TestContent2")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeComment teatimeComment = teatimeCommentRepository.save(TeatimeComment.builder()
                .content("TestContent")
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build());

        final String url =
                "/api/v1/teatimes/" + teatimeBoard2.getId() + "/deactivated-comments/" + teatimeComment.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("deactivateTeatimeComment 실패 - 비활성화된 회원이 댓글 삭제를 요청한 경우")
    @Test
    public void deactivateTeatimeComment_deactivatedUser_Fail() throws Exception {
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

        TeatimeComment teatimeComment = teatimeCommentRepository.save(TeatimeComment.builder()
                .content("TestContent")
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build());

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/deactivated-comments/" + teatimeComment.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isUnauthorized());
    }
}
