package com.example.restea.teatime.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.dto.TeatimeReplyCreationRequest;
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
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ContextConfiguration(classes = ResteaApplication.class)
@AutoConfigureMockMvc
public class CreateTeatimeReplyTest {
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
    public CreateTeatimeReplyTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
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

    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("내용이 100자 초과", "a".repeat(101)),
                Arguments.of("내용이 비어있음", ""),
                Arguments.of("내용이 null", null)
        );
    }

    @Transactional
    @BeforeEach
    public void mockMvcSetup() {
        teatimeReplyRepository.deleteAll();
        teatimeCommentRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
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

    @Test
    @DisplayName("createTeatimeReply : 대댓글 생성 성공")
    public void createTeatimeReply_Success() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + teatimeComment.getId() + "/replies";
        final String content = "replyContent";
        final TeatimeReplyCreationRequest teatimeReplyCreationRequest = new TeatimeReplyCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(teatimeReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());
        List<TeatimeReply> teatimeReplies = teatimeReplyRepository.findAll();
        assertThat(teatimeReplies.size()).isEqualTo(1);
        assertThat(teatimeReplies.get(0).getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("createTeatimeReply : 대댓글 생성 실패 - 댓글이 존재하지 않음")
    public void createTeatimeReply_CommentNotFound_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + "999" + "/replies";
        final String replyContent = "replyContent";
        final TeatimeReplyCreationRequest teatimeReplyCreationRequest = new TeatimeReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(teatimeReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<TeatimeReply> teatimeReplies = teatimeReplyRepository.findAll();
        assertThat(teatimeReplies.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeReply : 대댓글 생성 실패 - 댓글이 비활성화됨")
    @Test
    public void createTeatimeReply_DeactivatedComment_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        teatimeComment.deactivate();
        teatimeCommentRepository.save(teatimeComment);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + teatimeComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final TeatimeReplyCreationRequest teatimeReplyCreationRequest = new TeatimeReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(teatimeReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<TeatimeReply> teatimeReplies = teatimeReplyRepository.findAll();
        assertThat(teatimeReplies.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeReply : 대댓글 생성 실패 - 게시글이 비활성화됨")
    @Test
    public void createTeatimeReply_DeactivatedBoard_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        teatimeBoard = teatimeBoardRepository.findById(teatimeBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));
        teatimeBoard.deactivate();
        teatimeBoardRepository.save(teatimeBoard);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + teatimeComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final TeatimeReplyCreationRequest teatimeReplyCreationRequest = new TeatimeReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(teatimeReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<TeatimeReply> teatimeReplies = teatimeReplyRepository.findAll();
        assertThat(teatimeReplies.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeReply : 대댓글 생성 실패 - 게시글 작성자가 탈퇴함")
    @Test
    public void createTeatimeReply_DeactivatedBoardWriter_Fail() throws Exception {

        // given
        customOAuth2User = customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User boardWriter = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(boardWriter);

        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);

        boardWriter.deactivate();
        userRepository.save(boardWriter);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + teatimeComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final TeatimeReplyCreationRequest teatimeReplyCreationRequest = new TeatimeReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(teatimeReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<TeatimeReply> teatimeReplies = teatimeReplyRepository.findAll();
        assertThat(teatimeReplies.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeReply : 대댓글 생성 실패 - 대댓글 작성할 사용자가 탈퇴함")
    @Test
    public void createTeatimeReply_DeactivatedReplyWriter_Fail() throws Exception {

        // given
        customOAuth2User = customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User boardWriter = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(boardWriter);

        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);

        user.deactivate();
        userRepository.save(user);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + teatimeComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final TeatimeReplyCreationRequest teatimeReplyCreationRequest = new TeatimeReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(teatimeReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isUnauthorized());
        List<TeatimeReply> teatimeReplies = teatimeReplyRepository.findAll();
        assertThat(teatimeReplies.size()).isEqualTo(0);
    }

    @DisplayName("createTeatimeReply : 대댓글 생성 실패 - 게시글이 존재하지 않음")
    @Test
    public void createTeatimeReply_TeatimeBoardNotFound_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);

        final String url =
                "/api/v1/teatimes/" + (teatimeBoard.getId() + 999) + "/comments/" + teatimeComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final TeatimeReplyCreationRequest teatimeReplyCreationRequest = new TeatimeReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(teatimeReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<TeatimeReply> teatimeReplies = teatimeReplyRepository.findAll();
        assertThat(teatimeReplies.size()).isEqualTo(0);
    }

    @ParameterizedTest(name = "{index} : {0}")
    @MethodSource("invalidParameter")
    @DisplayName("createTeatimeReply : 대댓글 생성 실패 - 잘못된 content 입력")
    public void createTeatimeReply_Fail(String testName, String replyContent) throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + teatimeComment.getId() + "/replies";
        final TeatimeReplyCreationRequest teatimeReplyCreationRequest = new TeatimeReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(teatimeReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<TeatimeReply> teatimeReplies = teatimeReplyRepository.findAll();
        assertThat(teatimeReplies.size()).isEqualTo(0);
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
