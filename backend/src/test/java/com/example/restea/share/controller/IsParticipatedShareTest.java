package com.example.restea.share.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.repository.ShareBoardRepository;
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
class IsParticipatedShareTest {

    private final WebApplicationContext context;
    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public IsParticipatedShareTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                   WebApplicationContext context, ShareBoardRepository
                                           shareBoardRepository, UserRepository userRepository,
                                   CustomOAuth2UserService custumOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.shareBoardRepository = shareBoardRepository;
        this.userRepository = userRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
    }

    @Transactional
    @BeforeEach
    public void mockMvcSetUp() {
        userRepository.deleteAll();
        shareBoardRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

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

    @DisplayName("나눔 참가 여부 조회 성공 - 참가한 나눔 (글쓴이)")
    @Test
    public void isParticipate_writer_success() throws Exception {
        // given
        User writer = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(writer, 3);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants/" + writer.getId();

        // when
        ResultActions result = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.data.boardId").value(shareBoard.getId()));
        result.andExpect(jsonPath("$.data.userId").value(writer.getId()));
        result.andExpect(jsonPath("$.data.participated").value(true));
    }

    @DisplayName("나눔 참가 여부 조회 성공 - 참가한 나눔 (글쓴이)")
    @Test
    public void isParticipate_user_success() throws Exception {
        // given
        custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User writer = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        ShareBoard shareBoard = createShareBoard(writer, 3);

        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants/" + user.getId();

        // when
        ResultActions result = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.data.boardId").value(shareBoard.getId()));
        result.andExpect(jsonPath("$.data.userId").value(user.getId()));
        result.andExpect(jsonPath("$.data.participated").value(false));
    }
    // 나눔 참가 여부 조회 성공 - 참가한 나눔 (참가자)
    // 나눔 참가 여부 조회 성공 - 참가하지 않은 나눔
    // 나눔 참가 여부 조회 실패 - 유효하지 않는 사용자
    // 나눔 참가 여부 조회 실패 - 유효하지 않는 나눔
    // 나눔 참가 여부 조회 실패 - 다른 사람에 대한 조회

    private ShareBoard createShareBoard(User writer, Integer maxParticipants) {
        final String title = "Title";
        final String shareBoardContent = "Content";
        final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L);
        return shareBoardRepository.save(ShareBoard.builder()
                .title(title)
                .content(shareBoardContent)
                .maxParticipants(maxParticipants)
                .endDate(endDate)
                .user(writer)
                .build());
    }
}