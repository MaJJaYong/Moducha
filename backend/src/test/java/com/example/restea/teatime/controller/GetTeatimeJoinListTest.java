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
public class GetTeatimeJoinListTest {

    private final WebApplicationContext context;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeParticipantRepository teatimeParticipantRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public GetTeatimeJoinListTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
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
    @DisplayName("getTeatimeJoinList : 티타임 게시판 참가자 목록 조회 성공")
    public void getTeatimeJoinList_Success() throws Exception {

        // given
        List<TeatimeParticipant> participants = new ArrayList<>();

        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        addParticipants(participants, teatimeBoard);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";

        // when
        ResultActions resultActions = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isOk());

        String jsonResponse = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);

        for (int i = 0; i < 5; i++) {
            JsonNode item = jsonNode.path("data").path(i);
            TeatimeParticipant expectedParticipant = participants.get(i);

            assertThat(item.path("participantId").asInt()).isEqualTo(expectedParticipant.getId());
            assertThat(item.path("name").asText()).isEqualTo(expectedParticipant.getName());
            assertThat(item.path("phone").asText()).isEqualTo(expectedParticipant.getPhone());
            assertThat(item.path("address").asText()).isEqualTo(expectedParticipant.getAddress());
            assertThat(item.path("userId").asInt()).isEqualTo(expectedParticipant.getUser().getId());
            assertThat(item.path("boardId").asInt()).isEqualTo(teatimeBoard.getId());
            assertThat(item.path("nickname").asText()).isEqualTo(expectedParticipant.getUser().getNickname());
        }
    }

    @Test
    @DisplayName("getTeatimeJoinList 실패 : 티타임 게시판 작성자 아닌 경우")
    public void getTeatimeJoinList_Forbidden_Fail() throws Exception {

        // given
        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";

        // when
        ResultActions resultActions = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("getTeatimeJoinList 실패 : 티타임 게시판이 비활성화된 경우")
    public void getTeatimeJoinList_DeactivatedTeatimeBoard_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeBoard deactivatedTeatimeBoard = teatimeBoardRepository.findById(teatimeBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));

        deactivatedTeatimeBoard.deactivate();
        teatimeBoardRepository.save(deactivatedTeatimeBoard);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";

        // when
        ResultActions resultActions = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    private void addParticipants(List<TeatimeParticipant> participants, TeatimeBoard teatimeBoard) {
        for (int i = 0; i < 5; i++) {
            User user = User.builder()
                    .nickname("otherUser" + i)
                    .authId("otherAuthId" + i)
                    .build();

            userRepository.save(user);

            final String name = "name" + i;
            final String phone = "phone" + i;
            final String address = "address" + i;
            participants.add(teatimeParticipantRepository.save(TeatimeParticipant.builder()
                    .name(name)
                    .phone(phone)
                    .address(address)
                    .teatimeBoard(teatimeBoard)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
