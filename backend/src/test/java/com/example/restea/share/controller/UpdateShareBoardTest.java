package com.example.restea.share.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.dto.ShareUpdateRequest;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
public class UpdateShareBoardTest {

    private final WebApplicationContext context;
    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    private final ShareParticipantRepository shareParticipantRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public UpdateShareBoardTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                WebApplicationContext context,
                                ShareBoardRepository shareBoardRepository, UserRepository userRepository
            , CustomOAuth2UserService custumOAuth2UserService, ShareParticipantRepository shareParticipantRepository) {
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

    @DisplayName("updateShareBoard : 공유 게시글 수정에 성공한다.")
    @Test
    public void updateShare_Success() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        final String title = "Title";
        final String content = "Content";
        final Integer maxParticipants = 10;
        final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L);
        ShareBoard shareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title(title)
                .content(content)
                .maxParticipants(maxParticipants)
                .endDate(endDate)
                .user(user)
                .build());

        final String url = "/api/v1/shares/" + shareBoard.getId();
        final String updatedTitle = "UpdateTitle";
        final String updatedContent = "UpdateContent";
        final Integer updatedMaxParticipants = 20;
        final LocalDateTime updatedEndDate = LocalDateTime.now().plusWeeks(2L);
        final ShareUpdateRequest shareUpdateRequest = new ShareUpdateRequest(
                updatedTitle, updatedContent, updatedEndDate, updatedMaxParticipants, new ArrayList<>()
        );
        final String requestBody = objectMapper.writeValueAsString(shareUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());
        ShareBoard updatedShareBoard = shareBoardRepository.findById(shareBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));
        assertThat(updatedShareBoard.getTitle()).isEqualTo(updatedTitle);
        assertThat(updatedShareBoard.getContent()).isEqualTo(updatedContent);
        assertThat(updatedShareBoard.getMaxParticipants()).isEqualTo(updatedMaxParticipants);
        assertThat(updatedShareBoard.getEndDate().format(formatter)).isEqualTo(updatedEndDate.format(formatter));
    }

    @DisplayName("updateShareBoard 실패 - 존재하지 않는 게시글")
    @Test
    public void updateShare_NotFound_Fail() throws Exception {

        // given
        final String url = "/api/v1/shares/999";
        final String updatedTitle = "UpdateTitle";
        final String updatedContent = "UpdateContent";
        final Integer updatedMaxParticipants = 20;
        final LocalDateTime updatedEndDate = LocalDateTime.now().plusWeeks(2L);
        final ShareUpdateRequest shareUpdateRequest = new ShareUpdateRequest(
                updatedTitle, updatedContent, updatedEndDate, updatedMaxParticipants, new ArrayList<>()
        );
        final String requestBody = objectMapper.writeValueAsString(shareUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("updateShareBoard 실패 - 비활성화된 게시글")
    @Test
    public void updateShare_Deactivated_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        final String title = "Title";
        final String content = "Content";
        final Integer maxParticipants = 10;
        final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L);
        ShareBoard shareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title(title)
                .content(content)
                .maxParticipants(maxParticipants)
                .endDate(endDate)
                .user(user)
                .build());

        ShareBoard deactivatedShareBoard = shareBoardRepository.findById(shareBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));
        deactivatedShareBoard.deactivate();
        shareBoardRepository.save(deactivatedShareBoard);

        final String url = "/api/v1/shares/" + shareBoard.getId();
        final String updatedTitle = "UpdateTitle";
        final String updatedContent = "UpdateContent";
        final Integer updatedMaxParticipants = 20;
        final LocalDateTime updatedEndDate = LocalDateTime.now().plusWeeks(2L);
        final ShareUpdateRequest shareUpdateRequest = new ShareUpdateRequest(
                updatedTitle, updatedContent, updatedEndDate, updatedMaxParticipants, new ArrayList<>()
        );
        final String requestBody = objectMapper.writeValueAsString(shareUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("updateShareBoard 실패 - 권한이 없는 사용자")
    @Test
    public void updateShare_Unauthorized_Fail() throws Exception {

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

        final String url = "/api/v1/shares/" + shareBoard.getId();
        final String updatedTitle = "UpdateTitle";
        final String updatedContent = "UpdateContent";
        final Integer updatedMaxParticipants = 20;
        final LocalDateTime updatedEndDate = LocalDateTime.now().plusWeeks(2L);
        final ShareUpdateRequest shareUpdateRequest = new ShareUpdateRequest(
                updatedTitle, updatedContent, updatedEndDate, updatedMaxParticipants, new ArrayList<>()
        );
        final String requestBody = objectMapper.writeValueAsString(shareUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("updateShareBoard 실패 - 현재 신청자보다 작은 maxParticipants로 수정")
    @Test
    public void updateShare_LessThanCurrentParticipants_Fail() throws Exception {

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

        // 참여자 추가
        User participant;
        for (int i = 0; i < 3; i++) {
            custumOAuth2UserService.handleNewUser("authId" + i, "authToken" + i, "picture");
            participant = userRepository.findByAuthIdAndActivated("authId" + i, true)
                    .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

            shareParticipantRepository.save(ShareParticipant.builder()
                    .name("TestName" + i)
                    .phone("010-1234-000" + i)
                    .address("TestAddress" + i)
                    .shareBoard(shareBoard)
                    .user(participant)
                    .build());
        }

        final String url = "/api/v1/shares/" + shareBoard.getId();
        final String updatedTitle = "Title";
        final String updatedContent = "Content";
        final Integer updatedMaxParticipants = 1;
        final LocalDateTime updatedEndDate = LocalDateTime.now().plusWeeks(2L);
        final ShareUpdateRequest shareUpdateRequest = new ShareUpdateRequest(
                updatedTitle, updatedContent, updatedEndDate, updatedMaxParticipants, new ArrayList<>()
        );
        final String requestBody = objectMapper.writeValueAsString(shareUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    @DisplayName("updateShareBoard 실패 - 오늘보다 이전 날짜로 수정")
    @Test
    public void updateShare_BeforeThanToday_Fail() throws Exception {

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

        final String url = "/api/v1/shares/" + shareBoard.getId();
        final String updatedTitle = "Title";
        final String updatedContent = "Content";
        final Integer updatedMaxParticipants = 10;
        final LocalDateTime updatedEndDate = LocalDateTime.now().minusDays(1);
        final ShareUpdateRequest shareUpdateRequest = new ShareUpdateRequest(
                updatedTitle, updatedContent, updatedEndDate, updatedMaxParticipants, new ArrayList<>()
        );
        final String requestBody = objectMapper.writeValueAsString(shareUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }
}