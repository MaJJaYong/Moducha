package com.example.restea.share.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareParticipant;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.share.repository.ShareParticipantRepository;
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
class CancelShareTest {

    private final WebApplicationContext context;
    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final ShareParticipantRepository shareParticipantRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public CancelShareTest(MockMvc mockMvc, ObjectMapper objectMapper,
                           WebApplicationContext context, ShareBoardRepository
                                   shareBoardRepository, UserRepository userRepository,
                           ShareParticipantRepository shareParticipantRepository,
                           CustomOAuth2UserService custumOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.shareBoardRepository = shareBoardRepository;
        this.userRepository = userRepository;
        this.shareParticipantRepository = shareParticipantRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
    }

    @Transactional
    @BeforeEach
    public void mockMvcSetUp() {
        userRepository.deleteAll();
        shareBoardRepository.deleteAll();
        shareParticipantRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @BeforeEach
    public void OAuth2UserSetup() {
        customOAuth2User = custumOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        shareParticipantRepository.deleteAll();
        shareBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("나눔 참가 취소 성공")
    @Test
    public void cancel_success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(3, LocalDateTime.now().plusDays(1));
        ShareParticipant shareParticipant = ShareParticipant.builder()
                .name("name")
                .phone("phone")
                .address("address")
                .shareBoard(shareBoard)
                .user(user)
                .build();
        shareParticipantRepository.save(shareParticipant);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants/" + user.getId();

        // when
        ResultActions resultActions = mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.userId").value(user.getId()));
        resultActions.andExpect(jsonPath("$.data.boardId").value(shareBoard.getId()));
        assertThat(shareParticipantRepository.findAll()).isEmpty();
    }

    // 나눔 참가 취소 실패 - 다른 사람에 대한 취소
    // 나눔 참가 취소 실패 - 유효하지 않은 사용자
    // 나눔 참가 취소 실패 - 유효하지 않은 나눔
    // 나눔 참가 취소 실패 - 참가 마감된 나눔
    // 나눔 참가 취소 실패 - 글쓴이

    private ShareBoard createShareBoard(Integer maxParticipants, LocalDateTime endDate) {
        custumOAuth2UserService.handleNewUser("authId-writer", "authToken-writer", "picture");
        User writer = userRepository.findByAuthIdAndActivated("authId-writer", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        final String title = "Title";
        final String shareBoardContent = "Content";
        return shareBoardRepository.save(ShareBoard.builder()
                .title(title)
                .content(shareBoardContent)
                .maxParticipants(maxParticipants)
                .endDate(endDate)
                .user(writer)
                .build());
    }

}