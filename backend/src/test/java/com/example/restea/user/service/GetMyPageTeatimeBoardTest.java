package com.example.restea.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
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
public class GetMyPageTeatimeBoardTest {

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

    /**
     * testName, Page, perPage, contentsCount
     */
    private static Stream<Arguments> validParameter() {
        return Stream.of(
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
                Arguments.of("page가 null", null, "1"),
                Arguments.of("page가 0", "0", "1"),
                Arguments.of("page가 음수", "-1", "1"),
                Arguments.of("page가 문자열인 경우", "String", "1"),
                Arguments.of("page가 float인 경우", "2.456", "1"),
                Arguments.of("page가 blank인 경우", "  ", "1"),
                Arguments.of("page가 empty인 경우", "", "1"),
                Arguments.of("page가 Long인 경우", "2_147_483_648", "1"),

                Arguments.of("perPage가 null", "1", null),
                Arguments.of("perPage가 0", "1", "0"),
                Arguments.of("perPage가 음수", "1", "-1"),
                Arguments.of("perPage가 문자열인 경우", "1", "String"),
                Arguments.of("page가 float인 경우", "1", "2.456"),
                Arguments.of("page가 blank인 경우", "1", "  "),
                Arguments.of("page가 empty인 경우", "1", ""),
                Arguments.of("page가 Long인 경우", "1", "2_147_483_648")
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
    @DisplayName("[OK] getMyPageTeatimeBoard : 티타임 게시판 내가 작성한 글목록 가져오기 - 최신순")
    void 올바른_getMyPageTeatimeBoard_테스트(String testName, Integer page, Integer perPage, Integer contentsCount)
            throws Exception {
        // given
        List<TeatimeBoard> myTotalTeatimeBoards = new ArrayList<>();

        writeUserTeatimeBoard(myTotalTeatimeBoards, testUser, contentsCount);
        writeOtherUserTeatimeBoard(contentsCount);

        final String url = "/api/v1/users/" + testUser.getId() + "/mypage/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
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

        // 최신순으로 myTotalTeatimeBoards를 정렬
        myTotalTeatimeBoards.sort(
                (b1, b2) -> b2.getCreatedDate().compareTo(b1.getCreatedDate()));

        // 보여줄 컨텐츠 수 vs perPage 중 작은 것으로...
        int compareCounts = Math.min(myTotalTeatimeBoards.size() - (page - 1) * perPage, perPage);

        // perPage개수 만큼 테스트
        for (int i = 0; i < compareCounts; i++) {
            JsonNode item = jsonNode.path("data").path(i);
            TeatimeBoard expectedBoard = myTotalTeatimeBoards.get((page - 1) * perPage + i);

            assertThat(item.path("boardId").asText()).isEqualTo(expectedBoard.getId().toString());
            assertThat(item.path("title").asText()).isEqualTo(expectedBoard.getTitle());
            assertThat(item.path("maxParticipants").asInt()).isEqualTo(expectedBoard.getMaxParticipants());
            assertThat(item.path("participants").asInt()).isEqualTo(0);
            assertThat(item.path("nickname").asText()).isEqualTo(this.testUser.getNickname());
            assertThat(item.path("viewCount").asInt()).isEqualTo(0);

            // 최신순 정렬 되어있는지 확인
            if (i > 0) {
                JsonNode prevItem = jsonNode.path("data").path(i - 1);
                assertThat(item.path("createdDate").asText()).isLessThanOrEqualTo(
                        prevItem.path("createdDate").asText());
            }
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("noContentParameter")
    @DisplayName("[NoContent] getMyPageTeatimeBoard : 티타임 게시판 내가 작성한 글목록 가져오기 - 최신순")
    void NoContent_getMyPageTeatimeBoard_테스트(String testName, Integer page, Integer perPage)
            throws Exception {
        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/mypage/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
                .param("page", String.valueOf(page))
                .param("perPage", String.valueOf(perPage))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidParameter")
    @DisplayName("[BadRequest] getMyPageTeatimeBoard : 티타임 게시판 내가 작성한 글목록 가져오기 - 최신순")
    void invalid_getMyPageTeatimeBoard_테스트(String testName, String page, String perPage)
            throws Exception {
        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/mypage/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
                .param("page", page)
                .param("perPage", perPage)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
    }


    private void writeUserTeatimeBoard(List<TeatimeBoard> teatimeBoards, User user, Integer contentsCount) {
        for (int i = 0; i < contentsCount; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            final LocalDateTime broadcastDate = LocalDateTime.now().plusWeeks(2L + i);
            teatimeBoards.add(teatimeBoardRepository.save(TeatimeBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .broadcastDate(broadcastDate)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeOtherUserTeatimeBoard(Integer contentsCount) {
        User otherUser = User.builder()
                .nickname("otherUser")
                .authId("otherAuthId")
                .build();

        userRepository.save(otherUser);

        for (int i = 0; i < contentsCount; i++) {
            final String title = "Other Title" + i;
            final String content = "Other Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            final LocalDateTime broadcastDate = LocalDateTime.now().plusWeeks(2L + i);
            teatimeBoardRepository.save(TeatimeBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .broadcastDate(broadcastDate)
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
