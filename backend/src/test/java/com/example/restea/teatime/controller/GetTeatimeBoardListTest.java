package com.example.restea.teatime.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ContextConfiguration(classes = ResteaApplication.class)
@AutoConfigureMockMvc
class GetTeatimeBoardListTest {
    private final WebApplicationContext context;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public GetTeatimeBoardListTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
                                   TeatimeBoardRepository teatimeBoardRepository, UserRepository userRepository,
                                   CustomOAuth2UserService customOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.userRepository = userRepository;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    private static Stream<Arguments> keywordSearchParameters() {
        return Stream.of(
                Arguments.of("title", "Title1", 1, 5, 1),
                Arguments.of("content", "Content2", 1, 5, 1),
                Arguments.of("title", "Title", 1, 5, 5)
        );
    }

    @Transactional
    @BeforeEach
    public void mockMvcSetUp() {
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
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("getTeatimeBoardList : 티타임 게시판 목록 조회 성공.")
    @Test
    public void getTeatimeBoardList_10_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<TeatimeBoard> teatimeBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            final LocalDateTime broadcastDate = LocalDateTime.now().plusWeeks(1L + 2 * i);
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
        final String url = "/api/v1/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
                .param("perPage", "10")
                .param("page", "1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(10));
        for (int i = 0; i < 10; i++) {
            resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(teatimeBoards.get(9 - i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].title").value(teatimeBoards.get(9 - i).getTitle()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            teatimeBoards.get(9 - i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }

    // TODO : 티타임 게시판이 1개일 때 (최신 순)
    // TODO : 아무런 티타임 게시판이 없을 때 (최신 순)
    // TODO : 활성화된 티타임 게시판이 없을 때 (최신 순)
    // TODO : endDate가 지난 티타임 게시판만 있을 때 (최신 순)

    @DisplayName("getTeatimeBoardList : 티타임 게시판 목록 조회 성공 - perPage가 5일 때 (최신 순)")
    @Test
    public void getTeatimeBoardList_5_latest_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<TeatimeBoard> teatimeBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            final LocalDateTime broadcastDate = LocalDateTime.now().plusWeeks(1L + 2 * i);
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
        final String url = "/api/v1/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
                .param("perPage", "5")
                .param("page", "1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(5));
        for (int i = 0; i < 5; i++) {
            resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(teatimeBoards.get(9 - i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].title").value(teatimeBoards.get(9 - i).getTitle()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            teatimeBoards.get(9 - i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }

    // TODO : 일부 endDate가 지난 티타임 게시판이 있을 때 (임박 순)
    // TODO : 티타임 게시판이 1개일 때 (임박 순)
    // TODO : 아무런 티타임 게시판이 없을 때 (임박 순)
    // TODO : 활성화된 티타임 게시판이 없을 때 (임박 순)
    // TODO : endDate가 지난 티타임 게시판만 있을 때 (임박 순)

    // TODO : sort가 null일 때
    // TODO : sort가 latest 또는 urgent가 아닐 때

    // TODO : perPage 음수
    // TODO : perPage 0
    // TODO : perPage가 9999일 때
    // TODO : perPage가 null일 때

    // TODO : page가 음수일 때
    // TODO : page가 0일 때
    // TODO : page가 9999일 때
    // TODO : page가 null일 때

    @DisplayName("getTeatimeBoardList : 티타임 게시판 목록 조회 성공 - perPage가 5일 때 (임박 순)")
    @Test
    public void getTeatimeBoardList_5_urgent_success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<TeatimeBoard> teatimeBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            final LocalDateTime broadcastDate = LocalDateTime.now().plusWeeks(1L + 2 * i);
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
        final String url = "/api/v1/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "urgent")
                .param("perPage", "5")
                .param("page", "1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(5));
        for (int i = 0; i < 5; i++) {
            resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(teatimeBoards.get(i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].title").value(teatimeBoards.get(i).getTitle()))
//                    .andExpect(jsonPath("$.data[" + i + "].endDate").value(teatimeBoards.get(i).getEndDate()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            teatimeBoards.get(i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }

    @DisplayName("getTeatimeBoardList : 키워드 검색 테스트")
    @ParameterizedTest(name = "{index} => searchBy={0}, keyword={1}, page={2}, perPage={3}, expectedCount={4}")
    @MethodSource("keywordSearchParameters")
    public void getTeatimeBoardList_keywordSearch(String searchBy, String keyword, int page, int perPage,
                                                  int expectedCount) throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<TeatimeBoard> teatimeBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            final LocalDateTime broadcastDate = LocalDateTime.now().plusWeeks(1L + 2 * i);
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

        final String url = "/api/v1/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
                .param("perPage", String.valueOf(perPage))
                .param("page", String.valueOf(page))
                .param("searchBy", searchBy)
                .param("keyword", keyword)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(expectedCount));
        for (int i = 0; i < expectedCount; i++) {
            TeatimeBoard expectedBoard = teatimeBoards.stream()
                    .filter(board -> (searchBy.equals("title") && board.getTitle().contains(keyword)) ||
                            (searchBy.equals("content") && board.getContent().contains(keyword)))
                    .sorted((b1, b2) -> b2.getCreatedDate().compareTo(b1.getCreatedDate()))
                    .skip((long) (page - 1) * perPage)
                    .skip(i)
                    .findFirst()
                    .orElse(null);
            if (expectedBoard != null) {
                resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(expectedBoard.getId()))
                        .andExpect(jsonPath("$.data[" + i + "].title").value(expectedBoard.getTitle()))
                        .andExpect(
                                jsonPath("$.data[" + i + "].maxParticipants").value(expectedBoard.getMaxParticipants()))
                        .andExpect(jsonPath("$.data[" + i + "].participants").value(0))
                        .andExpect(jsonPath("$.data[" + i + "].nickname").value(user.getNickname()))
                        .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
            }
        }
    }

    @DisplayName("getTeatimeBoardList : endDate 임박 순으로 정렬된 데이터와 키워드 및 searchBy 검색")
    @ParameterizedTest(name = "{index} => searchBy={0}, keyword={1}, page={2}, perPage={3}, expectedCount={4}")
    @MethodSource("keywordSearchParameters")
    public void getTeatimeBoardList_sortedByEndDate_withKeywordAndSearchBy(String searchBy, String keyword, int page,
                                                                           int perPage, int expectedCount)
            throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        // Prepare test data
        List<TeatimeBoard> teatimeBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "SpecialTitle" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusDays(10 - i);
            final LocalDateTime broadcastDate = LocalDateTime.now().plusDays(10 - i);
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

        String url = "/api/v1/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "urgent")
                .param("perPage", String.valueOf(perPage))
                .param("page", String.valueOf(page))
                .param("searchBy", searchBy)
                .param("keyword", keyword)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.data.length()").value(expectedCount));
        for (int i = 0; i < expectedCount; i++) {
            TeatimeBoard expectedBoard = teatimeBoards.stream()
                    .filter(board -> (searchBy.equals("title") && board.getTitle().contains(keyword)) ||
                            (searchBy.equals("content") && board.getContent().contains(keyword)))
                    .sorted((b1, b2) -> -b2.getEndDate().compareTo(b1.getEndDate()))
                    .skip((long) (page - 1) * perPage)
                    .skip(i)
                    .findFirst()
                    .orElse(null);
            if (expectedBoard != null) {
                resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(expectedBoard.getId()))
                        .andExpect(jsonPath("$.data[" + i + "].title").value(expectedBoard.getTitle()))
                        .andExpect(
                                jsonPath("$.data[" + i + "].maxParticipants").value(expectedBoard.getMaxParticipants()))
                        .andExpect(jsonPath("$.data[" + i + "].participants").value(0))
                        .andExpect(jsonPath("$.data[" + i + "].nickname").value(user.getNickname()))
                        .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
            }
        }
    }

    @Test
    @DisplayName("getTeatimeBoardList : 잘못된 검색 기준")
    public void getTeatimeBoardList_InvalidSearchBy_Fail() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        // Prepare test data
        List<TeatimeBoard> teatimeBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "SpecialTitle" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusDays(10 - i);
            final LocalDateTime broadcastDate = LocalDateTime.now().plusDays(10 - i);
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

        String url = "/api/v1/teatimes";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "urgent")
                .param("perPage", "2")
                .param("page", "1")
                .param("searchBy", "search")
                .param("keyword", "keyword")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
    }
}