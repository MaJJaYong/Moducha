package com.example.restea.teatime.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeParticipant;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeParticipantRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ContextConfiguration(classes = ResteaApplication.class)
@AutoConfigureMockMvc
public class JoinCheckParticipantTest {

    private final WebApplicationContext context;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeParticipantRepository teatimeParticipantRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public JoinCheckParticipantTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
                                    TeatimeBoardRepository teatimeBoardRepository,
                                    TeatimeParticipantRepository teatimeParticipantRepository,
                                    UserRepository userRepository, CustomOAuth2UserService customOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.teatimeParticipantRepository = teatimeParticipantRepository;
        this.userRepository = userRepository;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Transactional
    @BeforeEach
    public void mockMvcSetUp() {
        teatimeParticipantRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @BeforeEach
    public void OAuth2UserSetup() {
        customOAuth2User = customOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        teatimeParticipantRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[OK] joinCheckParticipant : 참가 여부 조회 - true")
    void joinCheckParticipant_Success_True() throws Exception {
        // given
        User testUser = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeParticipant teatimeParticipant = teatimeParticipantRepository.save(TeatimeParticipant.builder()
                .name("name")
                .phone("phone")
                .address("address")
                .teatimeBoard(teatimeBoard)
                .user(testUser)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants/" + testUser.getId();

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk());

        String jsonResponse = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);

        assertThat(jsonNode.path("data").path("participated").asText()).isEqualTo("true");
    }

    @Test
    @DisplayName("[OK] joinCheckParticipant : 참가 여부 조회 - false")
    void joinCheckParticipant_Success_False() throws Exception {
        // given
        User testUser = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants/" + testUser.getId();

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk());

        String jsonResponse = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);

        assertThat(jsonNode.path("data").path("participated").asText()).isEqualTo("false");
    }

    @Test
    @DisplayName("[OK] joinCheckParticipant : 참가 여부 조회 - true(작성자)")
    void joinCheckParticipant_Success_True_Writer() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants/" + user.getId();

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk());

        String jsonResponse = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);

        assertThat(jsonNode.path("data").path("participated").asText()).isEqualTo("true");
    }
}
