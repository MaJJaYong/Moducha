package com.example.restea.share.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.dto.ShareCreationRequest;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
public class CreateShareBoardTest {

    private final WebApplicationContext context;
    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public CreateShareBoardTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                WebApplicationContext context,
                                ShareBoardRepository shareBoardRepository, UserRepository userRepository
            , CustomOAuth2UserService custumOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.shareBoardRepository = shareBoardRepository;
        this.userRepository = userRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
    }

    // nickname : "TestUser", authId : "authId", authToken : "authToken"
    @Transactional
    @BeforeEach
    public void mockMvcSetUp() {
        userRepository.deleteAll();
        shareBoardRepository.deleteAll();
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
        shareBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("createShare : 공유 게시글 생성에 성공한다.")
    @Test
    public void createShare_Success() throws Exception {

        // given
        final String url = "/api/v1/shares";
        final String title = "TestTitle";
        final String content = "TestContent";
        final ShareCreationRequest shareCreationRequest = new ShareCreationRequest(title, content,
                LocalDateTime.now().plusWeeks(1L), 10, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(shareCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());
        List<ShareBoard> shareBoards = shareBoardRepository.findAll();
        assertThat(shareBoards.size()).isEqualTo(1);
        assertThat(shareBoards.get(0).getTitle()).isEqualTo(title);
        assertThat(shareBoards.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("createShareBoard : 입력값이 null인 경우.")
    @Test
    public void createShareBoard_NullContent_Failure() throws Exception {
        // given
        final String url = "/api/v1/shares";
        final String title = "TestTitle";
        final ShareCreationRequest shareCreationRequest = new ShareCreationRequest(title, null,
                LocalDateTime.now().plusWeeks(1L), 10, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(shareCreationRequest);
        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));
        // then
        result.andExpect(status().isBadRequest());
        List<ShareBoard> shareBoards = shareBoardRepository.findAll();
        assertThat(shareBoards.size()).isEqualTo(0);
    }

    @DisplayName("createShareBoard : 제목이 비었을 때 실패한다.")
    @Test
    public void createShareBoard_EmptyTitle_Failure() throws Exception {
        // given
        final String url = "/api/v1/shares";
        final String title = "";
        final String content = "TestContent";
        final ShareCreationRequest shareCreationRequest = new ShareCreationRequest(title, content,
                LocalDateTime.now().plusWeeks(1L), 10, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(shareCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<ShareBoard> shareBoards = shareBoardRepository.findAll();
        assertThat(shareBoards.size()).isEqualTo(0);
    }

    @DisplayName("createShareBoard : 제목의 길이가 50을 초과할 때 실패한다.")
    @Test
    public void createShareBoard_OverTitleLength_Failure() throws Exception {
        // given
        final String url = "/api/v1/shares";
        final String title =
                "TestTitleTestTitleTestTitleTestTitleTestTitleTestTitleTestTitleTestTitleTestTitleTestTitle"
                        + "TestTitleTestTitleTestTitleTestTitleTestTitleTestTitleTestTitleTestTitleTestTitleTest"
                        + "TitleTestTitleTestTitle";
        final String content = "TestContent";
        final ShareCreationRequest shareCreationRequest = new ShareCreationRequest(title, content,
                LocalDateTime.now().plusWeeks(1L), 10, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(shareCreationRequest);
        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));
        // then
        result.andExpect(status().isBadRequest());
        List<ShareBoard> shareBoards = shareBoardRepository.findAll();
        assertThat(shareBoards.size()).isEqualTo(0);
    }

    @DisplayName("createShareBoard : content가 비었을 때 실패한다.")
    @Test
    public void createShareBoard_EmptyContent_Failure() throws Exception {
        // given
        final String url = "/api/v1/shares";
        final String title = "TestTitle";
        final String content = "";
        final ShareCreationRequest shareCreationRequest = new ShareCreationRequest(title, content,
                LocalDateTime.now().plusWeeks(1L), 10, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(shareCreationRequest);
        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));
        // then
        result.andExpect(status().isBadRequest());
        List<ShareBoard> shareBoards = shareBoardRepository.findAll();
        assertThat(shareBoards.size()).isEqualTo(0);
    }

    @DisplayName("createShareBoard : 참여자가 1명 미만일 때 실패한다. 0명")
    @Test
    public void createShareBoard_ZeroMaxParticipants_Failure() throws Exception {
        // given
        final String url = "/api/v1/shares";
        final String title = "TestTitle";
        final String content = "TestContent";
        final ShareCreationRequest shareCreationRequest = new ShareCreationRequest(title, content,
                LocalDateTime.now().plusWeeks(1L), 0, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(shareCreationRequest);
        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));
        // then
        result.andExpect(status().isBadRequest());
        List<ShareBoard> shareBoards = shareBoardRepository.findAll();
        assertThat(shareBoards.size()).isEqualTo(0);
    }

    @DisplayName("createShareBoard : 참여자가 1명 미만일 때 실패한다. -1명")
    @Test
    public void createShareBoard_MinusMaxParticipants_Failure() throws Exception {
        // given
        final String url = "/api/v1/shares";
        final String title = "TestTitle";
        final String content = "TestContent";
        final ShareCreationRequest shareCreationRequest = new ShareCreationRequest(title, content,
                LocalDateTime.now().plusWeeks(1L), -1, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(shareCreationRequest);
        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));
        // then
        result.andExpect(status().isBadRequest());
        List<ShareBoard> shareBoards = shareBoardRepository.findAll();
        assertThat(shareBoards.size()).isEqualTo(0);
    }

    @DisplayName("createShareBoard : 종료일이 현재 시간보다 이전일 때 실패한다.")
    @Test
    public void createShareBoard_InvalidEndDate_Failure() throws Exception {
        // given
        final String url = "/api/v1/shares";
        final String title = "TestTitle";
        final String content = "TestContent";
        final ShareCreationRequest shareCreationRequest = new ShareCreationRequest(title, content,
                LocalDateTime.now().minusDays(1L), 10, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(shareCreationRequest);
        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));
        // then
        result.andExpect(status().isBadRequest());
        List<ShareBoard> shareBoards = shareBoardRepository.findAll();
        assertThat(shareBoards.size()).isEqualTo(0);
    }
}
