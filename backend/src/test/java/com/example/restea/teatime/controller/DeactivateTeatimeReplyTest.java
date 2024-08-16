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
import com.example.restea.teatime.entity.TeatimeReply;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeCommentRepository;
import com.example.restea.teatime.repository.TeatimeReplyRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ContextConfiguration(classes = ResteaApplication.class)
@AutoConfigureMockMvc
public class DeactivateTeatimeReplyTest {
    private final WebApplicationContext context;
    private final UserRepository userRepository;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeCommentRepository teatimeCommentRepository;
    private final TeatimeReplyRepository teatimeReplyRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public DeactivateTeatimeReplyTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
                                      UserRepository userRepository, TeatimeBoardRepository teatimeBoardRepository,
                                      TeatimeCommentRepository teatimeCommentRepository,
                                      TeatimeReplyRepository teatimeReplyRepository,
                                      CustomOAuth2UserService customOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.context = context;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.teatimeCommentRepository = teatimeCommentRepository;
        this.teatimeReplyRepository = teatimeReplyRepository;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Transactional
    @BeforeEach
    public void mockMvcSetup() {
        userRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        teatimeCommentRepository.deleteAll();
        teatimeReplyRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).alwaysDo(print()).build();
    }

    @BeforeEach
    public void OAuth2UserSetup() {
        customOAuth2User = customOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        teatimeReplyRepository.deleteAll();
        teatimeCommentRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("deactivateTeatimeReply : 대댓글 비활성화 성공")
    @Test
    public void deactivateTeatimeReply_Success() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        TeatimeReply teatimeReply = teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/"
                + teatimeComment.getId() + "/deactivated-replies/" + teatimeReply.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isOk());
        TeatimeReply deactivatedTeatimeReply = teatimeReplyRepository.findById(teatimeReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        assertThat(deactivatedTeatimeReply.getActivated()).isFalse();
    }

    @DisplayName("deactivateTeatimeReply : 대댓글 비활성화 실패 - 존재하지 않는 대댓글")
    @Test
    public void deactivateTeatimeReply_ReplyNotFound_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/"
                + teatimeComment.getId() + "/deactivated-replies/" + 1;

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("deactivateTeatimeReply : 대댓글 비활성화 실패 - 이미 비활성화된 대댓글")
    @Test
    public void deactivateTeatimeReply_AlreadyDeactivated_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        TeatimeReply teatimeReply = teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());
        teatimeReply = teatimeReplyRepository.findById(teatimeReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        teatimeReply.deactivate();
        teatimeReplyRepository.save(teatimeReply);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/"
                + teatimeComment.getId() + "/deactivated-replies/" + (teatimeReply.getId());

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
        TeatimeReply deactivatedTeatimeReply = teatimeReplyRepository.findById(teatimeReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        assertThat(deactivatedTeatimeReply.getActivated()).isFalse();
    }

    @DisplayName("deactivateTeatimeReply : 대댓글 비활성화 실패 - 존재하지 않는 댓글")
    @Test
    public void deactivateTeatimeReply_CommentNotFound_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        TeatimeReply teatimeReply = teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/"
                + (teatimeComment.getId() + 999) + "/deactivated-replies/" + (teatimeReply.getId());

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
        TeatimeReply activatedTeatimeReply = teatimeReplyRepository.findById(teatimeReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        assertThat(activatedTeatimeReply.getActivated()).isTrue();
    }

    @DisplayName("deactivateTeatimeReply : 대댓글 비활성화 실패 - 존재하지 않는 게시글")
    @Test
    public void deactivateTeatimeReply_TeatimeBoardNotFound_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        TeatimeReply teatimeReply = teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + 999 + "/comments/"
                + teatimeComment.getId() + "/deactivated-replies/" + teatimeReply.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("deactivateTeatimeReply : 대댓글 비활성화 실패 - 권한이 없는 사용자")
    @Test
    public void deactivateTeatimeReply_Forbidden_Fail() throws Exception {

        // given
        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        TeatimeReply teatimeReply = teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/"
                + teatimeComment.getId() + "/deactivated-replies/" + teatimeReply.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("deactivateTeatimeReply : 대댓글 비활성화 실패 - 탈퇴한 사용자")
    @Test
    public void deactivateTeatimeReply_deactivatedUser_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        TeatimeReply teatimeReply = teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());

        user.deactivate();
        userRepository.save(user);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/"
                + teatimeComment.getId() + "/deactivated-replies/" + teatimeReply.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isUnauthorized());
    }

    private TeatimeBoard createTeatimeBoard(User user) {
        final String title = "Title";
        final String teatimeBoardContent = "Content";
        final Integer maxParticipants = 10;
        final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L);
        final LocalDateTime broadcastDate = LocalDateTime.now().plusWeeks(2L);
        return teatimeBoardRepository.save(TeatimeBoard.builder()
                .title(title)
                .content(teatimeBoardContent)
                .maxParticipants(maxParticipants)
                .endDate(endDate)
                .broadcastDate(broadcastDate)
                .user(user)
                .build());
    }

    private TeatimeComment createTeatimeComment(User user, TeatimeBoard teatimeBoard) {
        final String content = "commentContent";
        return teatimeCommentRepository.save(TeatimeComment.builder()
                .content(content)
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build());
    }
}
