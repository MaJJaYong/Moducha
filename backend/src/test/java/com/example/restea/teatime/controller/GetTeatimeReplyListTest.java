package com.example.restea.teatime.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.entity.TeatimeReply;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeCommentRepository;
import com.example.restea.teatime.repository.TeatimeReplyRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
@ContextConfiguration(classes = {ResteaApplication.class})
@AutoConfigureMockMvc
public class GetTeatimeReplyListTest {
    private final WebApplicationContext context;
    private final UserRepository userRepository;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeCommentRepository teatimeCommentRepository;
    private final TeatimeReplyRepository teatimeReplyRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public GetTeatimeReplyListTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
                                   UserRepository userRepository, TeatimeBoardRepository teatimeBoardRepository,
                                   TeatimeCommentRepository teatimeCommentRepository,
                                   TeatimeReplyRepository teatimeReplyRepository,
                                   CustomOAuth2UserService customOAuth2UserService) {
        this.mockMvc = mockMvc;
        this.context = context;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.teatimeBoardRepository = teatimeBoardRepository;
        this.teatimeCommentRepository = teatimeCommentRepository;
        this.teatimeReplyRepository = teatimeReplyRepository;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    private static Stream<Arguments> notEmptyValidParameter() {
        return Stream.of(
                Arguments.of("댓글이 50개 1page", 50, 1, 10, 10),
                Arguments.of("댓글이 50개 2page", 50, 2, 10, 10),
                Arguments.of("댓글이 28개 3page", 28, 3, 10, 8),
                Arguments.of("댓글이 1개", 1, 1, 10, 1)
        );
    }

    @Transactional
    @BeforeEach
    public void mockMvcSetup() {
        userRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        teatimeCommentRepository.deleteAll();
        teatimeReplyRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).alwaysDo(print()).build();
    }

    @BeforeEach
    public void OAuth2UserSetup() {
        customOAuth2User = customOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
    }

    @AfterEach
    public void tearDown() {
        teatimeReplyRepository.deleteAll();
        teatimeCommentRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("대댓글 목록 조회 성공 - 비어있지 않음")
    @ParameterizedTest(name = "{index} : {0}")
    @MethodSource("notEmptyValidParameter")
    public void getTeatimeReplyList_NotEmpty_Success(String testName, int num, int page, int perPage,
                                                     int expectedLength) throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);

        for (int i = 0; i < num; i++) {
            teatimeReplyRepository.save(TeatimeReply.builder()
                    .content("replyContent" + i)
                    .teatimeComment(teatimeComment)
                    .user(user)
                    .build());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + teatimeComment.getId() + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(page))
                .param("perPage", String.valueOf(perPage))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.length()").value(expectedLength));
        int index;
        int end = Math.min(num - (page - 1) * perPage, perPage);
        for (int i = 0; i < end; i++) {
            index = (page - 1) * perPage + i;
            resultActions.andExpect(jsonPath("$.data[" + i + "].content").value("replyContent" + index));
        }
    }

    @DisplayName("대댓글 목록 조회 성공 - 비어있음")
    @Test
    public void getTeatimeReplyList_NoContent_Success() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + teatimeComment.getId() + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(1))
                .param("perPage", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNoContent());
    }

    @DisplayName("대댓글 목록 조회 실패 - 존재하지 않는 댓글")
    @Test
    public void getTeatimeReplyList_CommentNotFount_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());

        final String url =
                "/api/v1/teatimes/" + teatimeBoard.getId() + "/comments/" + (teatimeComment.getId() + 999) + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(1))
                .param("perPage", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNotFound());
    }


    @DisplayName("대댓글 목록 조회 실패 - 존재하지 않는 게시글")
    @Test
    public void getTeatimeReplyList_BoardNotFount_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());

        final String url =
                "/api/v1/teatimes/" + 999 + "/comments/" + teatimeComment.getId() + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(1))
                .param("perPage", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("대댓글 조회 실패 - 게시글과 댓글이 각각 존재하나 연관되어 있지 않음")
    @Test
    public void getTeatimeReplyList_Fail() throws Exception {

        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));
        TeatimeBoard teatimeBoard = createTeatimeBoard(user);
        TeatimeComment teatimeComment = createTeatimeComment(user, teatimeBoard);
        teatimeReplyRepository.save(TeatimeReply.builder()
                .content("replyContent")
                .teatimeComment(teatimeComment)
                .user(user)
                .build());

        TeatimeBoard teatimeBoard2 = createTeatimeBoard(user);

        final String url =
                "/api/v1/teatimes/" + teatimeBoard2.getId() + "/comments/" + teatimeComment.getId() + "/replies";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .param("page", String.valueOf(1))
                .param("perPage", String.valueOf(10))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isNotFound());
    }

    private TeatimeBoard createTeatimeBoard(User user) {
        final String title = "Title";
        final String teatimeBoardContent = "Content";
        final Integer maxParticipants = 10;
        final LocalDateTime endDate = LocalDateTime.now().plusWeeks(1L);
        final LocalDateTime broadcastDate = LocalDateTime.now().plusWeeks(2L);
        return teatimeBoardRepository.save(TeatimeBoard.builder()
                .title(title)
                .content(teatimeBoardContent)
                .maxParticipants(maxParticipants)
                .endDate(endDate)
                .broadcastDate(broadcastDate)
                .user(user)
                .build());
    }

    private TeatimeComment createTeatimeComment(User user, TeatimeBoard teatimeBoard) {
        final String content = "commentContent";
        return teatimeCommentRepository.save(TeatimeComment.builder()
                .content(content)
                .teatimeBoard(teatimeBoard)
                .user(user)
                .build());
    }
}
