package com.example.restea.teatime.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@ContextConfiguration(classes = ResteaApplication.class)
@AutoConfigureMockMvc
public class GetTeatimeBoardTest {
    private final WebApplicationContext context;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public GetTeatimeBoardTest(MockMvc mockMvc, ObjectMapper objectMapper,
                               WebApplicationContext context,
                               TeatimeBoardRepository teatimeBoardRepository, UserRepository userRepository
            , CustomOAuth2UserService custumOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.userRepository = userRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
    }

    /**
     * testName, teatimeBoardId
     */
    private static Stream<Arguments> validParameter() {
        return Stream.of(
                Arguments.of("teatime_board_id가 있는 경우", 1)
        );
    }

    /**
     * testName, teatimeBoardId
     */
    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("제목이 null인 경우", 1),
                Arguments.of("내용이 null인 경우", 2)
        );
    }

    @Transactional
    @BeforeEach
    void setUp() {
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .alwaysDo(print())
                .build();
    }

    @BeforeEach
    public void OAuth2UserSetup() {
        customOAuth2User = custumOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("[OK] getTeatimeBoard : 티타임 게시판 조회")
    @Test
    public void getTeatimeBoard_Success() throws Exception {
        // given
        custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard createdTeatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + createdTeatimeBoard.getId();

        // when
        ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        System.out.println(jsonPath("$.data.endDate").toString());
        System.out.println(createdTeatimeBoard.getEndDate().toString());

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boardId").value(createdTeatimeBoard.getId()))
                .andExpect(jsonPath("$.data.title").value(createdTeatimeBoard.getTitle()))
                .andExpect(jsonPath("$.data.content").value(createdTeatimeBoard.getContent()))
                .andExpect(jsonPath("$.data.maxParticipants").value(createdTeatimeBoard.getMaxParticipants()))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(jsonPath("$.data.participants").value(0))
                .andExpect(jsonPath("$.data.nickname").value(user.getExposedNickname()));
    }

    @DisplayName("[OK] getTeatimeBoard : 티타임 게시판 조회 - 비활성화된 유저.")
    @Test
    public void getTeatimeBoard_deactivatedUser() throws Exception {
        // given
        custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard createdTeatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        // 유저 비활성화
        user.deactivate();
        userRepository.save(user);

        final String url = "/api/v1/teatimes/" + createdTeatimeBoard.getId();

        // when
        ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boardId").value(createdTeatimeBoard.getId()))
                .andExpect(jsonPath("$.data.title").value(createdTeatimeBoard.getTitle()))
                .andExpect(jsonPath("$.data.content").value(createdTeatimeBoard.getContent()))
                .andExpect(jsonPath("$.data.maxParticipants").value(createdTeatimeBoard.getMaxParticipants()))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(jsonPath("$.data.participants").value(0))
                .andExpect(jsonPath("$.data.nickname").value("탈퇴한 유저"));
    }

    @DisplayName("[NotFound] getTeatimeBoard : 존재하지 않는 티타임 게시판 조회로 인한 실패.")
    @Test
    public void getTeatimeBoard_fail() throws Exception {
        // given
        final String url = "/api/v1/teatimes/999";

        // when
        ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("[NotFound] getTeatimeBoard : 비활성화된 티타임 게시판 조회로 인한 실패.")
    @Test
    public void getTeatimeBoard_deactivated_fail() throws Exception {

        // given
        custumOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard createdTeatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        // DB에서 default 값들이 설정되는 관계로 다시 조회
        createdTeatimeBoard = teatimeBoardRepository.findById(createdTeatimeBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));

        // 게시글 비활성화
        createdTeatimeBoard.deactivate();
        teatimeBoardRepository.save(createdTeatimeBoard);

        final String url = "/api/v1/teatimes/" + createdTeatimeBoard.getId();

        // when
        ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));

        // then
        result.andExpect(status().isNotFound());
    }
}
