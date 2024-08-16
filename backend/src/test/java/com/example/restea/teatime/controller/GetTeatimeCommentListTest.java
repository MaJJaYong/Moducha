package com.example.restea.teatime.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeCommentRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.JsonNode;
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
public class GetTeatimeCommentListTest {
    private final WebApplicationContext context;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeCommentRepository teatimeCommentRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private User testUser;

    @Autowired
    public GetTeatimeCommentListTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                     WebApplicationContext context, TeatimeBoardRepository teatimeBoardRepository,
                                     TeatimeCommentRepository teatimeCommentRepository, UserRepository userRepository,
                                     CustomOAuth2UserService customOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.teatimeCommentRepository = teatimeCommentRepository;
        this.userRepository = userRepository;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    /**
     * testName, Page, perPage, contentsCount
     */
    private static Stream<Arguments> validParameter() {
        return Stream.of(
                Arguments.of("1page, 1perPage, 4totalContent", 1, 1, 4),
                Arguments.of("1page, 5perPage, 4totalContent", 1, 5, 4),
                Arguments.of("1page, 5perPage, 6totalContent", 1, 5, 6),
                Arguments.of("2page, 5perPage, 6totalContent", 2, 5, 6)
        );
    }

    /**
     * testName, Page, perPage
     */
    private static Stream<Arguments> noContentParameter() {
        return Stream.of(
                Arguments.of("2page, 5perPage, 4totalContent", 2, 5),
                Arguments.of("1page, 5perPage, 0totalContent", 1, 5)
        );
    }

    /**
     * testName, Page, perPage
     */
    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("page가 0", "0", "1"),
                Arguments.of("page가 음수", "-1", "1"),

                Arguments.of("perPage가 0", "1", "0"),
                Arguments.of("perPage가 음수", "1", "-1")
        );
    }

    /**
     * testName, Page, perPage
     */
    private static Stream<Arguments> ServerErrorParameter() {
        return Stream.of(
                Arguments.of("page가 null", null, "1"),
                Arguments.of("page가 문자열인 경우", "String", "1"),
                Arguments.of("page가 float인 경우", "2.456", "1"),
                Arguments.of("page가 blank인 경우", "  ", "1"),
                Arguments.of("page가 empty인 경우", "", "1"),
                Arguments.of("page가 Long인 경우", "2_147_483_648", "1"),

                Arguments.of("perPage가 null", "1", null),
                Arguments.of("perPage가 문자열인 경우", "1", "String"),
                Arguments.of("page가 float인 경우", "1", "2.456"),
                Arguments.of("page가 blank인 경우", "1", "  "),
                Arguments.of("page가 empty인 경우", "1", ""),
                Arguments.of("page가 Long인 경우", "1", "2_147_483_648")
        );
    }

    @Transactional
    @BeforeEach
    void setUp() {
        teatimeCommentRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .alwaysDo(print())
                .build();
    }

    @BeforeEach
    public void OAuth2UserSetup() {
        CustomOAuth2User customOAuth2User = customOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
        testUser = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
    }

    @AfterEach
    public void tearDown() {
        teatimeCommentRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("validParameter")
    @DisplayName("[OK] getTeatimeCommentList : 티타임 게시판 댓글 목록 조회")
    public void getTeatimeCommentList_Success(String testName, Integer page, Integer perPage, Integer contentsCount)
            throws Exception {
        // given
        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(testUser)
                .build());

        List<TeatimeComment> teatimeComments = new ArrayList<>();
        writeTeatimeComment(teatimeComments, testUser, teatimeBoard, contentsCount);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("perPage", perPage.toString())
                .param("page", page.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());

        String jsonResponse = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);

        int jsonDataLength = jsonNode.path("data").size();
        assertThat(jsonDataLength).isLessThanOrEqualTo(perPage);

        int compareCounts = Math.min(teatimeComments.size() - (page - 1) * perPage, perPage);

        for (int i = 0; i < compareCounts; i++) {
            JsonNode item = jsonNode.path("data").path(i);
            TeatimeComment expectedComment = teatimeComments.get((page - 1) * perPage + i);

            assertThat(item.path("commentId").asText()).isEqualTo(expectedComment.getId().toString());
            assertThat(item.path("boardId").asText()).isEqualTo(teatimeBoard.getId().toString());
            assertThat(item.path("content").asText()).isEqualTo(expectedComment.getContent());
            assertThat(item.path("userId").asInt()).isEqualTo(testUser.getId());
            assertThat(item.path("nickname").asText()).isEqualTo(this.testUser.getNickname());
            assertThat(item.path("replyCount").asInt()).isEqualTo(0);
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("noContentParameter")
    @DisplayName("[NotCoent] getTeatimeCommentList : 티타임 게시판 댓글 목록 조회")
    void getTeatimeCommentList_NoContent_Failure(String testName, Integer page, Integer perPage)
            throws Exception {
        // given
        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(testUser)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("perPage", perPage.toString())
                .param("page", page.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidParameter")
    @DisplayName("[BadRequest] getTeatimeCommentList : 티타임 게시판 댓글 목록 조회")
    void getTeatimeCommentList_BadRequest_Failure(String testName, String page, String perPage)
            throws Exception {
        // given
        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(testUser)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", page)
                .param("perPage", perPage)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ServerErrorParameter")
    @DisplayName("[ServerError] getTeatimeCommentList : 티타임 게시판 댓글 목록 조회")
    void getTeatimeCommentList_ServerError_Failure(String testName, String page, String perPage)
            throws Exception {
        // given
        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("TestTitle")
                .content("TestContent")
                .maxParticipants(10)
                .endDate(LocalDateTime.now().plusWeeks(1L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(testUser)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", page)
                .param("perPage", perPage)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().is5xxServerError());
    }

    private void writeTeatimeComment(List<TeatimeComment> teatimeComments, User user, TeatimeBoard teatimeBoard,
                                     Integer contentsCount) {
        for (int i = 0; i < contentsCount; i++) {
            final String content = "Content" + i;
            teatimeComments.add(teatimeCommentRepository.save(TeatimeComment.builder()
                    .content(content)
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
