package com.example.restea.share.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.dto.ShareReplyCreationRequest;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareComment;
import com.example.restea.share.entity.ShareReply;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.share.repository.ShareCommentRepository;
import com.example.restea.share.repository.ShareReplyRepository;
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
public class CreateShareReplyTest {
    private final WebApplicationContext context;
    private final UserRepository userRepository;
    private final ShareCommentRepository shareCommentRepository;
    private final ShareReplyRepository shareReplyRepository;
    private final ShareBoardRepository shareBoardRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public CreateShareReplyTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                WebApplicationContext context,
                                ShareBoardRepository shareBoardRepository,
                                ShareCommentRepository shareCommentRepository,
                                UserRepository userRepository,
                                ShareReplyRepository shareReplyRepository,
                                CustomOAuth2UserService custumOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.userRepository = userRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
        this.shareBoardRepository = shareBoardRepository;
        this.shareCommentRepository = shareCommentRepository;
        this.shareReplyRepository = shareReplyRepository;
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
        userRepository.deleteAll();
        shareBoardRepository.deleteAll();
        shareCommentRepository.deleteAll();
        shareReplyRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @BeforeEach
    public void OAuth2UserSetup() {
        customOAuth2User = custumOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        shareReplyRepository.deleteAll();
        shareCommentRepository.deleteAll();
        shareBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("createShareReply : 대댓글 생성 성공")
    @Test
    public void createShareReply_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + shareComment.getId() + "/replies";
        final String content = "replyContent";
        final ShareReplyCreationRequest shareReplyCreationRequest = new ShareReplyCreationRequest(content);
        final String requestBody = objectMapper.writeValueAsString(shareReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());
        List<ShareReply> shareReplies = shareReplyRepository.findAll();
        assertThat(shareReplies.size()).isEqualTo(1);
        assertThat(shareReplies.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("createShareReply : 대댓글 생성 실패 - 댓글이 존재하지 않음")
    @Test
    public void createShareReply_Fail_NoComment() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + "999" + "/replies";
        final String replyContent = "replyContent";
        final ShareReplyCreationRequest shareReplyCreationRequest = new ShareReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(shareReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<ShareReply> shareReplies = shareReplyRepository.findAll();
        assertThat(shareReplies.size()).isEqualTo(0);
    }

    @DisplayName("createShareReply : 대댓글 생성 실패 - 댓글이 비활성화됨")
    @Test
    public void createShareReply_Fail_DeactivatedComment() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        shareComment.deactivate();
        shareCommentRepository.save(shareComment);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + shareComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final ShareReplyCreationRequest shareReplyCreationRequest = new ShareReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(shareReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<ShareReply> shareReplies = shareReplyRepository.findAll();
        assertThat(shareReplies.size()).isEqualTo(0);
    }

    @DisplayName("createShareReply : 대댓글 생성 실패 - 게시글이 비활성화됨")
    @Test
    public void createShareReply_Fail_DeactivatedBoard() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        shareBoard = shareBoardRepository.findById(shareBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));
        shareBoard.deactivate();
        shareBoardRepository.save(shareBoard);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + shareComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final ShareReplyCreationRequest shareReplyCreationRequest = new ShareReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(shareReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<ShareReply> shareReplies = shareReplyRepository.findAll();
        assertThat(shareReplies.size()).isEqualTo(0);
    }

    @DisplayName("createShareReply : 대댓글 생성 실패 - 게시글 작성자가 탈퇴함")
    @Test
    public void createShareReply_Fail_DeactivatedBoardWriter() throws Exception {
        // given
        customOAuth2User = custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User boardWriter = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(boardWriter);

        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareComment shareComment = createShareComment(user, shareBoard);

        boardWriter.deactivate();
        userRepository.save(boardWriter);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + shareComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final ShareReplyCreationRequest shareReplyCreationRequest = new ShareReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(shareReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<ShareReply> shareReplies = shareReplyRepository.findAll();
        assertThat(shareReplies.size()).isEqualTo(0);
    }

    @DisplayName("createShareReply : 대댓글 생성 실패 - 대댓글 사용자가 탈퇴함")
    @Test
    public void createShareReply_Fail_DeactivatedReplyWriter() throws Exception {
        // given
        customOAuth2User = custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User boardWriter = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(boardWriter);

        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareComment shareComment = createShareComment(user, shareBoard);

        user.deactivate();
        userRepository.save(user);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + shareComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final ShareReplyCreationRequest shareReplyCreationRequest = new ShareReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(shareReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isUnauthorized());
        List<ShareReply> shareReplies = shareReplyRepository.findAll();
        assertThat(shareReplies.size()).isEqualTo(0);
    }

    @DisplayName("createShareReply : 대댓글 생성 실패 - 게시글이 존재하지 않음")
    @Test
    public void createShareReply_Fail_NoBoard() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);

        final String url =
                "/api/v1/shares/" + (shareBoard.getId() + 999) + "/comments/" + shareComment.getId() + "/replies";
        final String replyContent = "replyContent";
        final ShareReplyCreationRequest shareReplyCreationRequest = new ShareReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(shareReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<ShareReply> shareReplies = shareReplyRepository.findAll();
        assertThat(shareReplies.size()).isEqualTo(0);
    }

    @ParameterizedTest(name = "{index} : {0}")
    @MethodSource("invalidParameter")
    @DisplayName("createShareReply : 대댓글 생성 실패 - 잘못된 content 입력")
    public void createShareReply_Fail(String testName, String replyContent) throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + shareComment.getId() + "/replies";
        final ShareReplyCreationRequest shareReplyCreationRequest = new ShareReplyCreationRequest(replyContent);
        final String requestBody = objectMapper.writeValueAsString(shareReplyCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<ShareReply> shareReplies = shareReplyRepository.findAll();
        assertThat(shareReplies.size()).isEqualTo(0);
    }

    private ShareBoard createShareBoard(User user) {
        final String title = "Title";
        final String shareBoardContent = "Content";
        final Integer maxParticipants = 10;
        final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L);
        return shareBoardRepository.save(ShareBoard.builder()
                .title(title)
                .content(shareBoardContent)
                .maxParticipants(maxParticipants)
                .endDate(endDate)
                .user(user)
                .build());
    }

    private ShareComment createShareComment(User user, ShareBoard shareBoard) {
        final String content = "commentContent";
        return shareCommentRepository.save(ShareComment.builder()
                .content(content)
                .shareBoard(shareBoard)
                .user(user)
                .build());
    }

}
