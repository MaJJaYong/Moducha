package com.example.restea.share.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.entity.ShareBoard;
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
public class DeactivateShareBoardTest {

    private final WebApplicationContext context;
    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    private final ShareParticipantRepository shareParticipantRepository;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public DeactivateShareBoardTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
                                    ShareBoardRepository shareBoardRepository, UserRepository userRepository,
                                    CustomOAuth2UserService custumOAuth2UserService,
                                    ShareParticipantRepository shareParticipantRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.shareBoardRepository = shareBoardRepository;
        this.userRepository = userRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
        this.shareParticipantRepository = shareParticipantRepository;
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

    @DisplayName("deactivateShareBoard : 공유 게시글 비활성화에 성공한다.")
    @Test
    public void deactivateShare_Success() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .user(user)
                .build());

        final String url = "/api/v1/shares/deactivated-shares/" + shareBoard.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isOk());
        ShareBoard deactivatedShareBoard = shareBoardRepository.findById(shareBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));
        assertThat(deactivatedShareBoard.getActivated()).isFalse();
    }

    @DisplayName("deactivateShareBoard 실패 - 존재하지 않는 게시글")
    @Test
    public void deactivateShareBoard_NotFound_Fail() throws Exception {

        // given
        final String url = "/api/v1/shares/deactivated-shares/999";

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("deactivateShareBoard 실패 - 비활성화된 게시글")
    @Test
    public void deactivateShareBoard_Deactivated_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .user(user)
                .build());

        ShareBoard deactivatedShareBoard = shareBoardRepository.findById(shareBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));
        deactivatedShareBoard.deactivate();
        shareBoardRepository.save(deactivatedShareBoard);

        final String url = "/api/v1/shares/deactivated-shares/" + shareBoard.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("deactivateShareBoard 실패 - 권한이 없는 사용자")
    @Test
    public void deactivateShareBoard_Unauthorized_Fail() throws Exception {

        // given
        custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        ShareBoard shareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .user(user)
                .build());

        final String url = "/api/v1/shares/deactivated-shares/" + shareBoard.getId();

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isForbidden());
    }

}
