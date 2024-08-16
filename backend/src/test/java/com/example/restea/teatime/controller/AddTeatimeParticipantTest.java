package com.example.restea.teatime.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.teatime.dto.TeatimeJoinRequest;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeParticipant;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeParticipantRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@ContextConfiguration(classes = ResteaApplication.class)
@AutoConfigureMockMvc
public class AddTeatimeParticipantTest {
    private final WebApplicationContext context;
    private final TeatimeParticipantRepository teatimeParticipantRepository;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    private CustomOAuth2User customOAuth2User;

    @Autowired
    public AddTeatimeParticipantTest(MockMvc mockMvc, ObjectMapper objectMapper, WebApplicationContext context,
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

    /**
     * testName, naem, phone, address
     */
    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("이름이 null인 경우", null, "01012345678", "경상북도 구미시 어떤7길 어떤타운 123호"),
                Arguments.of("전화번호가 null인 경우", "홍길동", null, "경상북도 구미시 어떤7길 어떤타운 123호"),
                Arguments.of("주소가 null인 경우", "홍길동", "01012345678", null),

                Arguments.of("이름이 빈 경우", "", "01012345678", "경상북도 구미시 어떤7길 어떤타운 123호"),
                Arguments.of("전화번호가 빈 경우", "홍길동", "", "경상북도 구미시 어떤7길 어떤타운 123호"),
                Arguments.of("주소가 빈 경우", "홍길동", "01012345678", "")
        );
    }

    @BeforeEach
    public void setUp() {
        teatimeParticipantRepository.deleteAll();
        teatimeBoardRepository.deleteAll();
        userRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
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
    @DisplayName("[Create] addParticipant : 참가자 추가")
    void addParticipant_Success() throws Exception {
        // given
        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusDays(2L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";
        final String name = "홍길동";
        final String phone = "01012345678";
        final String address = "경상북도 구미시 어떤7길 어떤타운 123호";
        final TeatimeJoinRequest request = new TeatimeJoinRequest(name, phone, address);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());
        List<TeatimeParticipant> teatimeParticipants = teatimeParticipantRepository.findAll();
        assertThat(teatimeParticipants.size()).isEqualTo(1);
        assertThat(teatimeParticipants.get(0).getName()).isEqualTo(name);
        assertThat(teatimeParticipants.get(0).getPhone()).isEqualTo(phone);
        assertThat(teatimeParticipants.get(0).getAddress()).isEqualTo(address);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidParameter")
    @DisplayName("[BadRequest] addParticipant : 참가자 추가")
    void addParticipant_BadRequest_Failure(String testName, String name, String phone, String address)
            throws Exception {
        // given
        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusDays(2L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";
        final TeatimeJoinRequest request = new TeatimeJoinRequest(name, phone, address);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<TeatimeParticipant> teatimeParticipants = teatimeParticipantRepository.findAll();
        assertThat(teatimeParticipants.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("[Forbidden] addParticipant 실패 - 작성자인 경우")
    void addParticipant_Forbidden_Failure() throws Exception {
        // given
        User user = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusDays(2L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";
        final String name = "홍길동";
        final String phone = "01012345678";
        final String address = "경상북도 구미시 어떤7길 어떤타운 123호";
        final TeatimeJoinRequest request = new TeatimeJoinRequest(name, phone, address);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isForbidden());
        List<TeatimeParticipant> teatimeParticipants = teatimeParticipantRepository.findAll();
        assertThat(teatimeParticipants.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("[NotFound] addParticipant 실패 - 비활성화 된 게시글")
    void addParticipant_DeactivatedTeatimeBoard_Failure() throws Exception {
        // given
        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusDays(2L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        TeatimeBoard deactivatedTeatimeBoard = teatimeBoardRepository.findById(teatimeBoard.getId())
                .orElseThrow(() -> new RuntimeException("테스트를 위한 게시글 생성 실패"));
        deactivatedTeatimeBoard.deactivate();
        teatimeBoardRepository.save(deactivatedTeatimeBoard);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";
        final String name = "홍길동";
        final String phone = "01012345678";
        final String address = "경상북도 구미시 어떤7길 어떤타운 123호";
        final TeatimeJoinRequest request = new TeatimeJoinRequest(name, phone, address);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<TeatimeParticipant> teatimeParticipants = teatimeParticipantRepository.findAll();
        assertThat(teatimeParticipants.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("[BadRequest] addParticipant 실패 - 비활성화 된 게시글 작성자")
    void addParticipant_DeactivatedWriter_Failure() throws Exception {
        // given
        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusDays(2L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        user.deactivate();
        userRepository.save(user);

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";
        final String name = "홍길동";
        final String phone = "01012345678";
        final String address = "경상북도 구미시 어떤7길 어떤타운 123호";
        final TeatimeJoinRequest request = new TeatimeJoinRequest(name, phone, address);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
        List<TeatimeParticipant> teatimeParticipants = teatimeParticipantRepository.findAll();
        assertThat(teatimeParticipants.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("[Conflict] addParticipant 실패 - 이미 참석한 회원")
    void addParticipant_AlreadyParticipant_Failure() throws Exception {
        // given
        User testUser = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        customOAuth2UserService.handleNewUser("authId2", "authToken2", "picture");
        User user = userRepository.findByAuthIdAndActivated("authId2", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        TeatimeBoard teatimeBoard = teatimeBoardRepository.save(TeatimeBoard.builder()
                .title("title")
                .content("content")
                .maxParticipants(5)
                .endDate(LocalDateTime.now().plusDays(2L))
                .broadcastDate(LocalDateTime.now().plusWeeks(2L))
                .user(user)
                .build());

        final String name = "홍길동";
        final String phone = "01012345678";
        final String address = "경상북도 구미시 어떤7길 어떤타운 123호";
        TeatimeParticipant teatimeParticipant = teatimeParticipantRepository.save(TeatimeParticipant.builder()
                .name(name)
                .phone(phone)
                .address(address)
                .teatimeBoard(teatimeBoard)
                .user(testUser)
                .build());

        final String url = "/api/v1/teatimes/" + teatimeBoard.getId() + "/participants";
        final TeatimeJoinRequest request = new TeatimeJoinRequest(name, phone, address);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isConflict());
        List<TeatimeParticipant> teatimeParticipants = teatimeParticipantRepository.findAll();
        assertThat(teatimeParticipants.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("[NotFound] addParticipant 실패 - 존재하지 않는 게시글")
    void addParticipant_NotFoundTeatimeBoard_Failure() throws Exception {
        // given
        final String url = "/api/v1/teatimes/999/participants";
        final String name = "홍길동";
        final String phone = "01012345678";
        final String address = "경상북도 구미시 어떤7길 어떤타운 123호";
        final TeatimeJoinRequest request = new TeatimeJoinRequest(name, phone, address);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound());
        List<TeatimeParticipant> teatimeParticipants = teatimeParticipantRepository.findAll();
        assertThat(teatimeParticipants.size()).isEqualTo(0);
    }
}
