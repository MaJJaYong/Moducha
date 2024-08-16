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
import java.time.format.DateTimeFormatter;
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
public class GetShareBoardTest {

    private final WebApplicationContext context;
    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public GetShareBoardTest(MockMvc mockMvc, ObjectMapper objectMapper,
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

    @DisplayName("getShareBoard : 나눔 게시판 조회 성공.")
    @Test
    public void getShareBoard_Success() throws Exception {
        // given
        custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard createdShareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .user(user)
                .build());

        final String url = "/api/v1/shares/" + createdShareBoard.getId();

        // when
        ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boardId").value(createdShareBoard.getId()))
                .andExpect(jsonPath("$.data.title").value(createdShareBoard.getTitle()))
                .andExpect(jsonPath("$.data.content").value(createdShareBoard.getContent()))
                .andExpect(jsonPath("$.data.maxParticipants").value(createdShareBoard.getMaxParticipants()))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
//        저장하면서 형식이 약간 수정해서 완벽하게 일치하기 어렵다.
//        .andExpect(jsonPath("$.data.endDate").value(createdShareBoard.getEndDate().toString().substring(0, 26)))
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(jsonPath("$.data.participants").value(0))
                .andExpect(jsonPath("$.data.nickname").value(user.getExposedNickname()));
    }

    @DisplayName("getShareBoard : 나눔 게시판 조회 : 비활성화된 유저.")
    @Test
    public void getShareBoard_deactivatedUser() throws Exception {
        // given
        custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard createdShareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .user(user)
                .build());

        // 유저 비활성화
        user.deactivate();
        userRepository.save(user);

        final String url = "/api/v1/shares/" + createdShareBoard.getId();

        // when
        ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boardId").value(createdShareBoard.getId()))
                .andExpect(jsonPath("$.data.title").value(createdShareBoard.getTitle()))
                .andExpect(jsonPath("$.data.content").value(createdShareBoard.getContent()))
                .andExpect(jsonPath("$.data.maxParticipants").value(createdShareBoard.getMaxParticipants()))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
//        저장하면서 형식이 약간 수정해서 완벽하게 일치하기 어렵다.
//        .andExpect(jsonPath("$.data.endDate").value(createdShareBoard.getEndDate().toString().substring(0, 26)))
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(jsonPath("$.data.participants").value(0))
                .andExpect(jsonPath("$.data.nickname").value("탈퇴한 유저"));
    }

    @DisplayName("getShareBoard : 존재하지 않는 나눔 게시판 조회로 인한 실패.")
    @Test
    public void getShareBoard_fail() throws Exception {
        // given
        final String url = "/api/v1/shares/999";

        // when
        ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("getShareBoard : 비활성화된 나눔 게시판 조회로 인한 실패.")
    @Test
    public void getShareBoard_deactivated_fail() throws Exception {

        // given
        custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard createdShareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .user(user)
                .build());

        // DB에서 default 값들이 설정되는 관계로 다시 조회
        createdShareBoard = shareBoardRepository.findById(createdShareBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));

        // 게시글 비활성화
        createdShareBoard.deactivate();
        shareBoardRepository.save(createdShareBoard);

        final String url = "/api/v1/shares/" + createdShareBoard.getId();

        // when
        ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }
}
