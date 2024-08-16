package com.example.restea.share.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
@ContextConfiguration(classes = {ResteaApplication.class})
@AutoConfigureMockMvc
public class GetShareReplyListTest {
    private final WebApplicationContext context;
    private final UserRepository userRepository;
    private final ShareBoardRepository shareBoardRepository;
    private final ShareCommentRepository shareCommentRepository;
    private final ShareReplyRepository shareReplyRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public GetShareReplyListTest(MockMvc mockMvc, ObjectMapper objectMapper,
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
        this.shareBoardRepository = shareBoardRepository;
        this.shareCommentRepository = shareCommentRepository;
        this.shareReplyRepository = shareReplyRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
    }

    private static Stream<Arguments> notEmptyValidParameter() {
        return Stream.of(
                Arguments.of("댓글이 50개 1page", 50, 1, 10, 10),
                Arguments.of("댓글이 50개 2page", 50, 2, 10, 10),
                Arguments.of("댓글이 28개 3page", 28, 3, 10, 8),
                Arguments.of("댓글이 1개", 1, 1, 10, 1)
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

    @DisplayName("대댓글 목록 조회 성공 - 비어있지 않음")
    @ParameterizedTest(name = "{index} : {0}")
    @MethodSource("notEmptyValidParameter")
    public void getShareReplyListSuccessNotEmpty(String testName, int num, int page, int perPage, int expectedLength)
            throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);

        for (int i = 0; i < num; i++) {
            shareReplyRepository.save(ShareReply.builder()
                    .content("replyContent" + i)
                    .shareComment(shareComment)
                    .user(user)
                    .build());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + shareComment.getId() + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(page))
                .param("perPage", String.valueOf(perPage))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(expectedLength));
        int index;
        int end = Math.min(num - (page - 1) * perPage, perPage);
        for (int i = 0; i < end; i++) {
            index = (page - 1) * perPage + i;
            resultActions.andExpect(jsonPath("$.data[" + i + "].content").value("replyContent" + index));
        }
    }

    @DisplayName("대댓글 목록 조회 성공 - 비어있음")
    @Test
    public void getShareReplyListSuccessEmpty() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/" + shareComment.getId() + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(1))
                .param("perPage", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("대댓글 목록 조회 실패 - 존재하지 않는 댓글")
    @Test
    public void getShareReplyListFailNoComment() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        shareReplyRepository.save(ShareReply.builder()
                .content("replyContent")
                .shareComment(shareComment)
                .user(user)
                .build());

        final String url =
                "/api/v1/shares/" + shareBoard.getId() + "/comments/" + (shareComment.getId() + 999) + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(1))
                .param("perPage", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNotFound());
    }


    @DisplayName("대댓글 목록 조회 실패 - 존재하지 않는 게시글")
    @Test
    public void getShareReplyListFailNoBoard() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        shareReplyRepository.save(ShareReply.builder()
                .content("replyContent")
                .shareComment(shareComment)
                .user(user)
                .build());

        final String url =
                "/api/v1/shares/" + shareBoard.getId() + 999 + "/comments/" + shareComment.getId() + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(1))
                .param("perPage", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("대댓글 조회 실패 - 게시글과 댓글이 각각 존재하나 연관되어 있지 않음")
    @Test
    public void getShareReplyListFailBoard() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        shareReplyRepository.save(ShareReply.builder()
                .content("replyContent")
                .shareComment(shareComment)
                .user(user)
                .build());

        ShareBoard shareBoard2 = createShareBoard(user);

        final String url = "/api/v1/shares/" + shareBoard2.getId() + "/comments/" + shareComment.getId() + "/replies";
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
        assertThat(shareReplies.size()).isEqualTo(1);
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

