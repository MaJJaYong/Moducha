package com.example.restea.share.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
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
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
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
public class DeactivateShareReplyTest {
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
    public DeactivateShareReplyTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                    WebApplicationContext context,
                                    ShareBoardRepository shareBoardRepository,
                                    ShareCommentRepository shareCommentRepository,
                                    ShareReplyRepository shareReplyRepository,
                                    UserRepository userRepository
            , CustomOAuth2UserService custumOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.userRepository = userRepository;
        this.shareBoardRepository = shareBoardRepository;
        this.shareCommentRepository = shareCommentRepository;
        this.shareReplyRepository = shareReplyRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
    }

    private static Stream<Arguments> validParameter() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("")
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

    @DisplayName("deactivateShareReply : 대댓글 비활성화 성공")
    @Test
    public void deactivateShareReply_Success() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        ShareReply shareReply = shareReplyRepository.save(ShareReply.builder()
                .content("replyContent")
                .shareComment(shareComment)
                .user(user)
                .build());

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/"
                + shareComment.getId() + "/deactivated-replies/" + shareReply.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isOk());
        ShareReply deactivatedShareReply = shareReplyRepository.findById(shareReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        assertThat(deactivatedShareReply.getActivated()).isFalse();
    }

    @DisplayName("deactivateShareReply : 대댓글 비활성화 실패 - 존재하지 않는 대댓글")
    @Test
    public void deactivateShareReply_FailNoComment() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        ShareReply shareReply = shareReplyRepository.save(ShareReply.builder()
                .content("replyContent")
                .shareComment(shareComment)
                .user(user)
                .build());

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/"
                + shareComment.getId() + "/deactivated-replies/" + (shareReply.getId() + 1);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
        ShareReply activatedShareReply = shareReplyRepository.findById(shareReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        assertThat(activatedShareReply.getActivated()).isTrue();
    }

    @DisplayName("deactivateShareReply : 대댓글 비활성화 실패 - 이미 비활성화된 대댓글")
    @Test
    public void deactivateShareReply_Fail_AlreadyDeactivated() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        ShareReply shareReply = shareReplyRepository.save(ShareReply.builder()
                .content("replyContent")
                .shareComment(shareComment)
                .user(user)
                .build());
        shareReply = shareReplyRepository.findById(shareReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        shareReply.deactivate();
        shareReplyRepository.save(shareReply);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/"
                + shareComment.getId() + "/deactivated-replies/" + (shareReply.getId());

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
        ShareReply deactivatedShareReply = shareReplyRepository.findById(shareReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        assertThat(deactivatedShareReply.getActivated()).isFalse();
    }

    @DisplayName("deactivateShareReply : 대댓글 비활성화 실패 - 존재하지 않는 댓글")
    @Test
    public void deactivateShareReply_Fail_NoComment() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = createShareBoard(user);
        ShareComment shareComment = createShareComment(user, shareBoard);
        ShareReply shareReply = shareReplyRepository.save(ShareReply.builder()
                .content("replyContent")
                .shareComment(shareComment)
                .user(user)
                .build());

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments/"
                + (shareComment.getId() + 999) + "/deactivated-replies/" + (shareReply.getId());

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
        ShareReply activatedShareReply = shareReplyRepository.findById(shareReply.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 대댓글 생성 실패"));
        assertThat(activatedShareReply.getActivated()).isTrue();
    }

    // TODO : 대댓글 비활성화 실패 - 존재하지 않는 게시글

    // TODO : 대댓글 비활성화 실패 - 권한 없음

    // TODO : 대댓글 비활성화 실패 - 탈퇴한 사용자

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
