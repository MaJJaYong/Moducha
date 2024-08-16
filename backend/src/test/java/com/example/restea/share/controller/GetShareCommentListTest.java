package com.example.restea.share.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareComment;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.share.repository.ShareCommentRepository;
import com.example.restea.share.repository.ShareReplyRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
public class GetShareCommentListTest {
    private final WebApplicationContext context;
    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final ShareCommentRepository shareCommentRepository;
    private final ShareReplyRepository shareReplyRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public GetShareCommentListTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
                                   ShareBoardRepository shareBoardRepository, UserRepository userRepository,
                                   ShareCommentRepository shareCommentRepository,
                                   ShareReplyRepository shareReplyRepository,
                                   CustomOAuth2UserService custumOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.shareBoardRepository = shareBoardRepository;
        this.userRepository = userRepository;
        this.shareCommentRepository = shareCommentRepository;
        this.shareReplyRepository = shareReplyRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
    }

    // nickname : "TestUser", authId : "authId", authToken : "authToken"
    @Transactional
    @BeforeEach
    public void mockMvcSetUp() {
        userRepository.deleteAll();
        shareBoardRepository.deleteAll();
        shareCommentRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // mockMvc에서 @AuthenticationPrincipal CustomOAuth2User를 사용하기 위해
    @BeforeEach
    public void OAuth2UserSetup() {
        customOAuth2User = custumOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        shareCommentRepository.deleteAll();
        shareBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("getShareCommentList : 나눔 게시판 댓글 목록 조회 성공.")
    @Test
    public void getShareCommentList_10_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title("Title")
                .content("Content")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .build());

        List<ShareComment> shareComments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String content = "Content" + i;
            shareComments.add(shareCommentRepository.save(ShareComment.builder()
                    .content(content)
                    .shareBoard(shareBoard)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("perPage", "10")
                .param("page", "1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(10));
        for (int i = 0; i < 10; i++) {
            resultActions.andExpect(jsonPath("$.data[" + i + "].commentId").value(shareComments.get(i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].boardId").value(shareBoard.getId()))
                    .andExpect(jsonPath("$.data[" + i + "].content").value(shareComments.get(i).getContent()))
                    .andExpect(jsonPath("$.data[" + i + "].userId").value(user.getId()))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath(("$.data[" + i + "].replyCount")).value(0));
        }
    }

    @DisplayName("getShareCommentList : 나눔 게시판 댓글 목록 조회 성공 - perPage가 5일 때")
    @Test
    public void getShareBoardList_5_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title("Title")
                .content("Content")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .build());

        List<ShareComment> shareComments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String content = "Content" + i;
            shareComments.add(shareCommentRepository.save(ShareComment.builder()
                    .content(content)
                    .shareBoard(shareBoard)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares/" + shareBoard.getId() + "/comments";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("perPage", "5")
                .param("page", "1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(5));
        for (int i = 0; i < 5; i++) {
            resultActions.andExpect(jsonPath("$.data[" + i + "].commentId").value(shareComments.get(i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].boardId").value(shareBoard.getId()))
                    .andExpect(jsonPath("$.data[" + i + "].content").value(shareComments.get(i).getContent()))
                    .andExpect(jsonPath("$.data[" + i + "].userId").value(user.getId()))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath(("$.data[" + i + "].replyCount")).value(0));
        }
    }
}
