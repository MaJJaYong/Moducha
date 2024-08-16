package com.example.restea.share.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.share.entity.ShareBoard;
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
public class GetShareBoardListTest {

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
    public GetShareBoardListTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
                                 ShareBoardRepository shareBoardRepository, UserRepository userRepository,
                                 CustomOAuth2UserService custumOAuth2UserService,
                                 ShareParticipantRepository shareParticipantRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.context = context;
        this.shareBoardRepository = shareBoardRepository;
        this.userRepository = userRepository;
        this.custumOAuth2UserService = custumOAuth2UserService;
        this.shareParticipantRepository = shareParticipantRepository;
    }

    private static Stream<Arguments> ValidParameter() {
        return Stream.of(
                // String testName, int num, String sort, String perPage, String page
                Arguments.of("게시글이 0개, 페이지 당 5개, 1page", 0, "5", "1"),
                Arguments.of("게시글이 10개, 페이지 당 5개, 1page", 10, "5", "1"),
                Arguments.of("게시글이 10개, 페이지 당 3개, 2page", 10, "3", "2"),
                Arguments.of("게시글이 50개, 페이지 당 12개, 3page", 50, "12", "3")
        );
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

    @DisplayName("getShareBoardList : 나눔 게시판 목록 조회 성공 - perPage가 10일")
    @Test
    public void getShareBoardList_10_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<ShareBoard> shareBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            shareBoards.add(shareBoardRepository.save(ShareBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares";

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
            resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(shareBoards.get(9 - i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].title").value(shareBoards.get(9 - i).getTitle()))
                    .andExpect(jsonPath("$.data[" + i + "].content").value(shareBoards.get(9 - i).getContent()))
//                    .andExpect(jsonPath("$.data[" + i + "].endDate").value(shareBoards.get(9 - i).getEndDate()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            shareBoards.get(9 - i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }

    @DisplayName("getShareBoardList : 나눔 게시판 목록 조회 성공 - sort가 latest일 때")
    @ParameterizedTest(name = "i{index} : {0}")
    @MethodSource("ValidParameter")
    public void getShareBoardList_latest_Success(String testName, int num, String perPage, String page)
            throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<ShareBoard> shareBoards = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            shareBoards.add(shareBoardRepository.save(ShareBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
                .param("perPage", perPage)
                .param("page", page)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        int leftover = num - (Integer.parseInt(page) - 1) * Integer.parseInt(perPage);
        int end = Math.min(leftover, Integer.parseInt(perPage));
        resultActions.andExpect(num == 0 ? status().isNoContent() : status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(end));
        for (int i = 0; i < end; i++) {
            resultActions.andExpect(
                            jsonPath("$.data[" + i + "].boardId").value(shareBoards.get(leftover - 1 - i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].title").value(shareBoards.get(leftover - 1 - i).getTitle()))
                    .andExpect(
                            jsonPath("$.data[" + i + "].content").value(shareBoards.get(leftover - 1 - i).getContent()))
//                    .andExpect(jsonPath("$.data[" + i + "].endDate").value(shareBoards.get(9 - i).getEndDate()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            shareBoards.get(leftover - 1 - i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }

    @DisplayName("getShareBoardList : 나눔 게시판 목록 조회 성공 - sort가 urgent일 때")
    @ParameterizedTest(name = "i{index} : {0}")
    @MethodSource("ValidParameter")
    public void getShareBoardList_urgent_Success(String testName, int num, String perPage, String page)
            throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<ShareBoard> shareBoards = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            shareBoards.add(shareBoardRepository.save(ShareBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "urgent")
                .param("perPage", perPage)
                .param("page", page)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        int precedingElement = (Integer.parseInt(page) - 1) * Integer.parseInt(perPage);
        int end = Math.min(num - precedingElement, Integer.parseInt(perPage));
        resultActions.andExpect(num == 0 ? status().isNoContent() : status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(end));
        for (int i = 0; i < end; i++) {
            resultActions.andExpect(
                            jsonPath("$.data[" + i + "].boardId").value(shareBoards.get(precedingElement + i).getId()))
                    .andExpect(
                            jsonPath("$.data[" + i + "].title").value(shareBoards.get(precedingElement + i).getTitle()))
                    .andExpect(jsonPath("$.data[" + i + "].content").value(
                            shareBoards.get(precedingElement + i).getContent()))
//                    .andExpect(jsonPath("$.data[" + i + "].endDate").value(shareBoards.get(9 - i).getEndDate()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            shareBoards.get(precedingElement + i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }

    @DisplayName("getShareBoardList : 나눔 게시판 목록 조회 성공 - perPage가 5일 때 (최신 순)")
    @Test
    public void getShareBoardList_5_latest_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<ShareBoard> shareBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            shareBoards.add(shareBoardRepository.save(ShareBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares";

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
            resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(shareBoards.get(9 - i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].title").value(shareBoards.get(9 - i).getTitle()))
                    .andExpect(jsonPath("$.data[" + i + "].content").value(shareBoards.get(9 - i).getContent()))
//                    .andExpect(jsonPath("$.data[" + i + "].endDate").value(shareBoards.get(9 - i).getEndDate()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            shareBoards.get(9 - i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }

    @DisplayName("getShareBoardList : 나눔 게시판 목록 조회 성공 - perPage가 5일 때 (임박 순)")
    @Test
    public void getShareBoardList_5_urgent_success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<ShareBoard> shareBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            shareBoards.add(shareBoardRepository.save(ShareBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares";

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
            resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(shareBoards.get(i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].title").value(shareBoards.get(i).getTitle()))
                    .andExpect(jsonPath("$.data[" + i + "].content").value(shareBoards.get(i).getContent()))
//                    .andExpect(jsonPath("$.data[" + i + "].endDate").value(shareBoards.get(i).getEndDate()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            shareBoards.get(i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }

    @DisplayName("getShareBoardList : 나눔 게시판 목록 조회 성공 - perPage가 10일(title)")
    @Test
    public void search_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<ShareBoard> shareBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            shareBoards.add(shareBoardRepository.save(ShareBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
                .param("perPage", "10")
                .param("page", "1")
                .param("searchBy", "title")
                .param("keyword", "Title")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(10));
        for (int i = 0; i < 10; i++) {
            resultActions.andExpect(jsonPath("$.data[" + i + "].boardId").value(shareBoards.get(9 - i).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].title").value(shareBoards.get(9 - i).getTitle()))
                    .andExpect(jsonPath("$.data[" + i + "].content").value(shareBoards.get(9 - i).getContent()))
//                    .andExpect(jsonPath("$.data[" + i + "].endDate").value(shareBoards.get(9 - i).getEndDate()))
                    .andExpect(jsonPath("$.data[" + i + "].maxParticipants").value(
                            shareBoards.get(9 - i).getMaxParticipants()))
                    .andExpect(jsonPath(("$.data[" + i + "].participants")).value(0))
                    .andExpect(jsonPath(("$.data[" + i + "].nickname")).value(user.getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].viewCount").value(0));
        }
    }


    @DisplayName("getShareBoardList : 나눔 게시판 목록 검색 성공 - perPage가 10일(title)")
    @Test
    public void search_Success_2() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        List<ShareBoard> shareBoards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String title = "Title" + i;
            final String content = "Content" + i;
            final Integer maxParticipants = 10 + i;
            final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L + i);
            shareBoards.add(shareBoardRepository.save(ShareBoard.builder()
                    .title(title)
                    .content(content)
                    .maxParticipants(maxParticipants)
                    .endDate(endDate)
                    .user(user)
                    .build()));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final String url = "/api/v1/shares";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("sort", "latest")
                .param("perPage", "10")
                .param("page", "1")
                .param("searchBy", "title")
                .param("keyword", "Title2")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(1));
        resultActions.andExpect(jsonPath("$.data[0].boardId").value(shareBoards.get(2).getId()))
                .andExpect(jsonPath("$.data[0].title").value(shareBoards.get(2).getTitle()))
                .andExpect(jsonPath("$.data[0].content").value(shareBoards.get(2).getContent()))
                .andExpect(jsonPath("$.data[0].maxParticipants").value(
                        shareBoards.get(2).getMaxParticipants()))
                .andExpect(jsonPath(("$.data[0].participants")).value(0))
                .andExpect(jsonPath(("$.data[0].nickname")).value(user.getNickname()))
                .andExpect(jsonPath("$.data[0].viewCount").value(0));
    }

    //  나눔 게시판이 1개일 때 (최신 순)
    //  아무런 나눔 게시판이 없을 때 (최신 순)
    //  활성화된 나눔 게시판이 없을 때 (최신 순)
    //  endDate가 지난 나눔 게시판만 있을 때 (최신 순)

    //  일부 endDate가 지난 나눔 게시판이 있을 때 (임박 순)
    //  나눔 게시판이 1개일 때 (임박 순)
    //  아무런 나눔 게시판이 없을 때 (임박 순)
    //  활성화된 나눔 게시판이 없을 때 (임박 순)
    //  endDate가 지난 나눔 게시판만 있을 때 (임박 순)

    //  sort가 null일 때
    //  sort가 latest 또는 urgent가 아닐 때

    //  perPage 음수
    //  perPage 0
    //  perPage가 9999일 때
    //  perPage가 null일 때

    //  page가 음수일 때
    //  page가 0일 때
    //  page가 9999일 때
    //  page가 null일 때

}
