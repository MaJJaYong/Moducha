package com.example.restea.teatime.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.dto.TeatimeCreationRequest;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
public class CreateTeatimeBoardTest {
    private final WebApplicationContext context;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService custumOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public CreateTeatimeBoardTest(MockMvc mockMvc, ObjectMapper objectMapper,
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
     * testName, title, content, endDate, broadcastDate, maxParticipants
     */
    private static Stream<Arguments> validParameter() {
        return Stream.of(
                Arguments.of("맞는 값", "안녕하세요.", "차 나눔합니다.", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 4)
        );
    }

    /**
     * testName, title, content, endDate, broadcastDate, maxParticipants
     */
    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("제목이 null인 경우", null, "내용", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),
                Arguments.of("내용이 null인 경우", "제목", null, LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),
                Arguments.of("endDate가 null인 경우", "제목", "내용", null, LocalDateTime.now().plusWeeks(2L), 4),
                Arguments.of("broadcastDate가 null인 경우", "제목", "내용", LocalDateTime.now().plusWeeks(1L), null, 4),
                Arguments.of("maxParticipants가 null인 경우", "제목", "내용", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), null),

                Arguments.of("제목이 빈 경우", "", "내용", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),
                Arguments.of("내용이 빈 경우", "제목", "", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),

                Arguments.of("제목이 50 초과인 경우", "제목".repeat(50), "내용", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),

                Arguments.of("endDate가 현재보다 1초 전인 경우", "제목", "내용", LocalDateTime.now().minusSeconds(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),
                Arguments.of("endDate가 현재보다 1분 전인 경우", "제목", "내용", LocalDateTime.now().minusMinutes(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),
                Arguments.of("endDate가 현재보다 한시간 전인 경우", "제목", "내용", LocalDateTime.now().minusHours(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),
                Arguments.of("endDate가 현재보다 하루 전인 경우", "제목", "내용", LocalDateTime.now().minusDays(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),
                Arguments.of("endDate가 현재보다 일주일 전인 경우", "제목", "내용", LocalDateTime.now().minusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 4),

                Arguments.of("broadcastDate가 endDate보다 과거인 경우", "제목", "내용", LocalDateTime.now().minusWeeks(1L),
                        LocalDateTime.now().plusWeeks(1L).minusSeconds(1L), 4),
                Arguments.of("broadcastDate가 endDate와 같을 경우", "제목", "내용", LocalDateTime.now().minusWeeks(1L),
                        LocalDateTime.now().plusWeeks(1L), 4),

                Arguments.of("maxParticipants가 0인 경우", "제목", "내용", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 0),
                Arguments.of("maxParticipants가 음수인 경우", "제목", "내용", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), -1),
                Arguments.of("maxParticipants가 7인 경우", "제목", "내용", LocalDateTime.now().plusWeeks(1L),
                        LocalDateTime.now().plusWeeks(2L), 7)

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

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("validParameter")
    @DisplayName("[OK] createTeatime : 티타임 게시판 글 작성")
    void createTeatime_Success(String testName, String title, String content, LocalDateTime endDate,
                               LocalDateTime broadcastDate,
                               Integer maxParticipants) throws Exception {
        // given
        final String url = "/api/v1/teatimes";
        final TeatimeCreationRequest teatimeCreationRequest = new TeatimeCreationRequest(title, content,
                endDate, broadcastDate, maxParticipants, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(teatimeCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());
        List<TeatimeBoard> teatimeBoards = teatimeBoardRepository.findAll();
        assertThat(teatimeBoards.size()).isEqualTo(1);
        assertThat(teatimeBoards.get(0).getTitle()).isEqualTo(title);
        assertThat(teatimeBoards.get(0).getContent()).isEqualTo(content);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidParameter")
    @DisplayName("[BadRequest] createTeatime : 티타임 게시판 글 작성")
    void createTeatime_Failure(String testName, String title, String content, LocalDateTime endDate,
                               LocalDateTime broadcastDate,
                               Integer maxParticipants) throws Exception {
        // given
        final String url = "/api/v1/teatimes";
        final TeatimeCreationRequest teatimeCreationRequest = new TeatimeCreationRequest(title, content,
                endDate, broadcastDate, maxParticipants, new ArrayList<>());
        final String requestBody = objectMapper.writeValueAsString(teatimeCreationRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<TeatimeBoard> teatimeBoards = teatimeBoardRepository.findAll();
        assertThat(teatimeBoards.size()).isEqualTo(0);
    }
}
