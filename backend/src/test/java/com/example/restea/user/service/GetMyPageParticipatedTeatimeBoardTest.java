package com.example.restea.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
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
public class GetMyPageParticipatedTeatimeBoardTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeatimeBoardRepository teatimeBoardRepository;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    private User testUser;
    @Autowired
    private TeatimeParticipantRepository teatimeParticipantRepository;

    /**
     * testName, Page, perPage, contentsCount, sort
     */
    private static Stream<Arguments> validParameter() {
        return Stream.of(
                Arguments.of("1page, 5perPage, 4totalContent, ongoing", 1, 5, 4, "ongoing"),
                Arguments.of("1page, 5perPage, 6totalContent, ongoing", 1, 5, 6, "ongoing"),
                Arguments.of("2page, 5perPage, 6totalContent, ongoing", 2, 5, 6, "ongoing"),

                Arguments.of("1page, 5perPage, 4totalContent, before", 1, 5, 4, "before"),
                Arguments.of("1page, 5perPage, 6totalContent, before", 1, 5, 6, "before"),
                Arguments.of("2page, 5perPage, 6totalContent, before", 2, 5, 6, "before")
        );
    }

    /**
     * testName, Page, perPage, sort
     */
    private static Stream<Arguments> noContentParameter() {
        return Stream.of(
                Arguments.of("2page, 5perPage, 0totalContent", 2, 5, "ongoing"),
                Arguments.of("2page, 5perPage, 0totalContent", 2, 5, "before")
        );
    }

    /**
     * testName, Page, perPage
     */
    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("page가 null", null, "1", "ongoing"),
                Arguments.of("page가 0", "0", "1", "ongoing"),
                Arguments.of("page가 음수", "-1", "1", "ongoing"),
                Arguments.of("page가 문자열인 경우", "String", "1", "ongoing"),
                Arguments.of("page가 float인 경우", "2.456", "1", "ongoing"),
                Arguments.of("page가 blank인 경우", "  ", "1", "ongoing"),
                Arguments.of("page가 empty인 경우", "", "1", "ongoing"),
                Arguments.of("page가 Long인 경우", "2_147_483_648", "1", "ongoing"),

                Arguments.of("perPage가 null", "1", null, "before"),
                Arguments.of("perPage가 0", "1", "0", "before"),
                Arguments.of("perPage가 음수", "1", "-1", "before"),
                Arguments.of("perPage가 문자열인 경우", "1", "String", "before"),
                Arguments.of("page가 float인 경우", "1", "2.456", "before"),
                Arguments.of("page가 blank인 경우", "1", "  ", "before"),
                Arguments.of("page가 empty인 경우", "1", "", "before"),
                Arguments.of("page가 Long인 경우", "1", "2_147_483_648", "before"),

                Arguments.of("sort가 올바르지 않을 경우", "1", "1", "범식"),
                Arguments.of("sort가 올바르지 않을 경우", "1", "1", "latest")
        );
    }

    /**
     * Spring 5.2 버전 이후로 기본 인코딩 문자는 UTF-8이 아님. -> 따라서 Charset을 UTF-8로 등록해야함.
     */
    @BeforeEach()
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .alwaysDo(print())
                .build();
    }

    @Transactional
    @BeforeEach
    void mockMvcSetUp() {
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // mockMvc에서 @AuthenticaionPrincipal CustomOAuth2User를 사용하기 위한 세팅
    @BeforeEach
    void OAuth2UserSetup() {
        CustomOAuth2User customOAuth2User = customOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
        testUser = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
    }

    @AfterEach
    void tearDown() {
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("validParameter")
    @DisplayName("[OK] getParticipatedTeatimeBoardList : 티타임게시판 내가 참여한 글목록 가져오기 - 가까운 방송일(Ongoing), 지난 방송일(before)")
    void 올바른_participated_getMyPageTeatimeBoard_테스트(String testName, Integer page, Integer perPage,
                                                    Integer contentsCount,
                                                    String sort) throws Exception {
        // given
        List<TeatimeBoard> myTotalTeatimeBoards = new ArrayList<>();

        writeUserTeatimeBoard(myTotalTeatimeBoards, testUser, contentsCount, sort);
        writeOtherUserTeatimeBoard(contentsCount);

        final String url = "/api/v1/users/" + testUser.getId() + "/mypage/participated-teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", sort)
                .param("page", String.valueOf(page))
                .param("perPage", String.valueOf(perPage))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());

        // Json response 파싱
        String jsonResponse = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);

        // perPage 개수보다 컨텐츠 개수가 적을 수 있음.
        int jsonDataLength = jsonNode.path("data").size();
        assertThat(jsonDataLength).isLessThanOrEqualTo(perPage);

        // ongoing : 오름차순, before : 내림차순
        if ("ongoing".equals(sort)) {
            myTotalTeatimeBoards.sort(
                    (b1, b2) -> b1.getBroadcastDate().compareTo(b2.getBroadcastDate()));
        }

        if ("before".equals(sort)) {
            myTotalTeatimeBoards.sort(
                    (b1, b2) -> b2.getBroadcastDate().compareTo(b1.getBroadcastDate()));
        }

        // 보여줄 컨텐츠 수 vs perPage 중 작은 것으로...
        int compareCounts = Math.min(myTotalTeatimeBoards.size() - (page - 1) * perPage, perPage);

        System.out.println(jsonNode.path("data").toString());

        // perPage개수 만큼 테스트
        for (int i = 0; i < compareCounts; i++) {
            JsonNode item = jsonNode.path("data").path(i);
            TeatimeBoard expectedBoard = myTotalTeatimeBoards.get((page - 1) * perPage + i);

            assertThat(item.path("boardId").asText()).isEqualTo(expectedBoard.getId().toString());
            assertThat(item.path("title").asText()).isEqualTo(expectedBoard.getTitle());
            assertThat(item.path("maxParticipants").asInt()).isEqualTo(expectedBoard.getMaxParticipants());
            assertThat(item.path("participants").asInt()).isEqualTo(1);
            assertThat(item.path("nickname").asText()).isEqualTo(this.testUser.getNickname());
            assertThat(item.path("viewCount").asInt()).isEqualTo(0);

            // ongoing : 오름차순, before : 내림차순
            if (i > 0) {
                if ("ongoing".equals(sort)) {
                    JsonNode prevItem = jsonNode.path("data").path(i - 1);
                    assertThat(item.path("broadcastDate").asText()).isGreaterThanOrEqualTo(
                            prevItem.path("broadcastDate").asText());
                }

                if ("before".equals(sort)) {
                    JsonNode prevItem = jsonNode.path("data").path(i - 1);
                    assertThat(item.path("broadcastDate").asText()).isLessThanOrEqualTo(
                            prevItem.path("broadcastDate").asText());
                }
            }
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("noContentParameter")
    @DisplayName("[NoContent] getParticipatedTeatimeBoardList : 티타임게시판 내가 참여한 글목록 가져오기 - 가까운 방송일(Ongoing), 지난 방송일(before)")
    void NoContent_getMyPageTeatimeBoard_테스트(String testName, Integer page, Integer perPage, String sort)
            throws Exception {
        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/mypage/participated-teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", sort)
                .param("page", String.valueOf(page))
                .param("perPage", String.valueOf(perPage))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidParameter")
    @DisplayName("[BadRequest] getParticipatedTeatimeBoardList : 티타임게시판 내가 참여한 글목록 가져오기 - 가까운 방송일(Ongoing), 지난 방송일(before)")
    void invalid_getMyPageTeatimeBoard_테스트(String testName, String page, String perPage, String sort)
            throws Exception {
        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/mypage/participated-teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", sort)
                .param("page", page)
                .param("perPage", perPage)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
    }


    public void writeUserTeatimeBoard(List<TeatimeBoard> teatimeBoards, User user, Integer contentsCount, String sort) {
        if ("ongoing".equals(sort)) {
            IntStream.range(0, contentsCount * 2).forEach(i -> {
                TeatimeBoard teatimeBoard = createTeatimeBoard(user, i, contentsCount);
                if (i >= contentsCount) {
                    teatimeBoards.add(teatimeBoard);
                }
            });
        }

        if ("before".equals(sort)) {
            IntStream.range(0, contentsCount).forEach(i -> {
                TeatimeBoard teatimeBoard = createTeatimeBoard(user, i, contentsCount);
                if (i < contentsCount) {
                    teatimeBoards.add(teatimeBoard);
                }
            });
        }
    }

    private TeatimeBoard createTeatimeBoard(User user, int index, int contentsCount) {
        TeatimeBoard teatimeBoard = TeatimeBoard.builder()
                .title("Title" + index)
                .content("Content" + index)
                .maxParticipants(10 + index)
                .endDate(calculateBroadcastDate(index, contentsCount).minusHours(6))
                .broadcastDate(calculateBroadcastDate(index, contentsCount))
                .user(user)
                .build();

        teatimeBoardRepository.save(teatimeBoard);

        TeatimeParticipant teatimeParticipant = TeatimeParticipant.builder()
                .name("name" + index)
                .phone("phone" + index)
                .address("address" + index)
                .user(user)
                .teatimeBoard(teatimeBoard)
                .build();

        teatimeParticipantRepository.save(teatimeParticipant);

        return teatimeBoard;
    }

    private LocalDateTime calculateBroadcastDate(int index, int contentsCount) {
        if (index < contentsCount) {
            return LocalDateTime.now().minusWeeks(1L + (contentsCount - index));
        } else {
            return LocalDateTime.now().plusWeeks(1L + (index - contentsCount));
        }
    }

    private void writeOtherUserTeatimeBoard(Integer contentsCount) {
        User otherUser = User.builder()
                .nickname("otherUser")
                .authId("otherAuthId")
                .build();

        userRepository.save(otherUser);

        for (int i = 0; i < contentsCount; i++) {
            teatimeBoardRepository.save(TeatimeBoard.builder()
                    .title("Other Title" + i)
                    .content("Other Content" + i)
                    .maxParticipants(10 + i)
                    .endDate(LocalDateTime.now().plusWeeks(1L + i).minusHours(6))
                    .broadcastDate(LocalDateTime.now().plusWeeks(1L + i))
                    .user(otherUser)
                    .build());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

