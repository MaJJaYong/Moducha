package com.example.restea.share.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.dto.ShareJoinRequest;
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
class ParticipateShareTest {

    private final WebApplicationContext context;
    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final ShareParticipantRepository shareParticipantRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public ParticipateShareTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                WebApplicationContext context,
                                ShareBoardRepository shareBoardRepository,
                                UserRepository userRepository,
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

    @DisplayName("participateShare : 나눔 참가 성공")
    @Test
    public void participate_success() throws Exception {
        // given
        ShareBoard shareBoard = createShareBoard(10, LocalDateTime.now().plusWeeks(1L));

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants";
        final ShareJoinRequest shareJoinRequest = new ShareJoinRequest("홍동길", "010-1234-5678",
                "경상북도 구미시 진평동 123-4 101호");
        final String requestBody = objectMapper.writeValueAsString(shareJoinRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());
        List<ShareParticipant> shareParticipants = shareParticipantRepository.findAll();
        assertThat(shareParticipants).hasSize(1);
        assertThat(shareParticipants.get(0).getName()).isEqualTo(shareJoinRequest.getName());
        assertThat(shareParticipants.get(0).getPhone()).isEqualTo(shareJoinRequest.getPhone());
        assertThat(shareParticipants.get(0).getAddress()).isEqualTo(shareJoinRequest.getAddress());
    }

    @DisplayName("participateShare : 나눔 참가 실패 - 이미 참가한 나눔")
    @Test
    public void participate_fail_already_participated() throws Exception {
        // given
        ShareBoard shareBoard = createShareBoard(10, LocalDateTime.now().plusWeeks(1L));

        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        shareParticipantRepository.save(ShareParticipant.builder()
                .name("홍동길")
                .phone("010-1234-5678")
                .address("경상북도 구미시 진평동 123-4 101호")
                .shareBoard(shareBoard)
                .user(user)
                .build());

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants";
        final ShareJoinRequest shareJoinRequest = new ShareJoinRequest("홍동길", "010-1234-5678",
                "경상북도 구미시 진평동 123-4 101호");
        final String requestBody = objectMapper.writeValueAsString(shareJoinRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<ShareParticipant> shareParticipants = shareParticipantRepository.findAll();
        assertThat(shareParticipants).hasSize(1);
    }

    @DisplayName("participateShare : 나눔 참가 실패 - 존재하지 않은 나눔")
    @Test
    public void participate_fail_not_exist_share() throws Exception {
        // given
        final String url = "/api/v1/shares/" + "999" + "/participants";
        final ShareJoinRequest shareJoinRequest = new ShareJoinRequest("홍동길", "010-1234-5678",
                "경상북도 구미시 진평동 123-4 101호");
        final String requestBody = objectMapper.writeValueAsString(shareJoinRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<ShareParticipant> shareParticipants = shareParticipantRepository.findAll();
        assertThat(shareParticipants).hasSize(0);
    }

    @DisplayName("participateShare : 나눔 참가 실패 - 유효하지 않은 사용자")
    @Test
    public void participate_fail_invalid_user() throws Exception {
        // given
        ShareBoard shareBoard = createShareBoard(10, LocalDateTime.now().plusWeeks(1L));

        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        user.deactivate();
        userRepository.save(user);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants";
        final ShareJoinRequest shareJoinRequest = new ShareJoinRequest("홍동길", "010-1234-5678",
                "경상북도 구미시 진평동 123-4 101호");
        final String requestBody = objectMapper.writeValueAsString(shareJoinRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isUnauthorized());
        List<ShareParticipant> shareParticipants = shareParticipantRepository.findAll();
        assertThat(shareParticipants).hasSize(0);
    }

    @DisplayName("participateShare : 나눔 참가 실패 - 참가 마감된 나눔")
    @Test
    public void participate_fail_end_date() throws Exception {
        // given
        ShareBoard shareBoard = createShareBoard(10, LocalDateTime.now().minusWeeks(1L));
        shareBoard = shareBoardRepository.findById(shareBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 나눔 생성 실패"));
        shareBoardRepository.save(shareBoard);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants";
        final ShareJoinRequest shareJoinRequest = new ShareJoinRequest("홍동길", "010-1234-5678",
                "경상북도 구미시 진평동 123-4 101호");
        final String requestBody = objectMapper.writeValueAsString(shareJoinRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<ShareParticipant> shareParticipants = shareParticipantRepository.findAll();
        assertThat(shareParticipants).hasSize(0);
    }

    @DisplayName("participateShare : 나눔 참가 실패 - 비활성화된 글쓴이")
    @Test
    public void participate_fail_deactivated_writer() throws Exception {
        // given
        ShareBoard shareBoard = createShareBoard(10, LocalDateTime.now().plusWeeks(1L));
        shareBoard.getUser().deactivate();
        userRepository.save(shareBoard.getUser());

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants";
        final ShareJoinRequest shareJoinRequest = new ShareJoinRequest("홍동길", "010-1234-5678",
                "경상북도 구미시 진평동 123-4 101호");
        final String requestBody = objectMapper.writeValueAsString(shareJoinRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<ShareParticipant> shareParticipants = shareParticipantRepository.findAll();
        assertThat(shareParticipants).hasSize(0);
    }

    @DisplayName("participateShare : 나눔 참가 실패 - 참가 인원 초과")
    @Test
    public void participate_fail_already_max() throws Exception {
        // given
        int maxParticipants = 3;
        ShareBoard shareBoard = createShareBoard(maxParticipants, LocalDateTime.now().plusWeeks(1L));
        createParticipant(shareBoard, maxParticipants);

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants";
        final ShareJoinRequest shareJoinRequest = new ShareJoinRequest("홍동길", "010-1234-5678",
                "경상북도 구미시 진평동 123-4 101호");
        final String requestBody = objectMapper.writeValueAsString(shareJoinRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<ShareParticipant> shareParticipants = shareParticipantRepository.findAll();
        assertThat(shareParticipants).hasSize(maxParticipants);
    }

    @DisplayName("participateShare : 나눔 참가 실패 - 글쓴이 본인이 참가")
    @Test
    public void participate_fail_user_is_writer() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        final String title = "Title";
        final String shareBoardContent = "Content";
        final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L);
        ShareBoard shareBoard = shareBoardRepository.save(ShareBoard.builder()
                .title(title)
                .content(shareBoardContent)
                .maxParticipants(3)
                .endDate(endDate)
                .user(user)
                .build());

        final String url = "/api/v1/shares/" + shareBoard.getId() + "/participants";
        final ShareJoinRequest shareJoinRequest = new ShareJoinRequest("홍동길", "010-1234-5678",
                "경상북도 구미시 진평동 123-4 101호");
        final String requestBody = objectMapper.writeValueAsString(shareJoinRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<ShareParticipant> shareParticipants = shareParticipantRepository.findAll();
        assertThat(shareParticipants).hasSize(0);
    }


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

    private void createParticipant(ShareBoard shareBoard, int count) {
        for (int i = 0; i < count; i++) {
            CustomOAuth2User customOAuth2User = custumOAuth2UserService.handleNewUser("authId" + i, "authToken" + i,
                    "picture");
            User user = userRepository.findByAuthIdAndActivated("authId" + i, true)
                    .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
            shareParticipantRepository.save(ShareParticipant.builder()
                    .name("홍동길" + i)
                    .phone("010-1234-5678")
                    .address("경상북도 구미시 진평동 123-4 101호")
                    .shareBoard(shareBoard)
                    .user(user)
                    .build());
        }
    }
}