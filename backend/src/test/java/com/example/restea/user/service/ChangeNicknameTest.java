package com.example.restea.user.service;

import static com.example.restea.oauth2.enums.TokenType.ACCESS;
import static com.example.restea.oauth2.enums.TokenType.REFRESH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restea.ResteaApplication;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.entity.RefreshToken;
import com.example.restea.oauth2.jwt.JWTUtil;
import com.example.restea.oauth2.repository.RefreshTokenRepository;
import com.example.restea.oauth2.service.CustomOAuth2UserService;
import com.example.restea.oauth2.service.RefreshTokenService;
import com.example.restea.user.dto.NicknameUpdateRequest;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.util.SecurityTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@ContextConfiguration(classes = ResteaApplication.class)
@AutoConfigureMockMvc
public class ChangeNicknameTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private Integer originalRefreshTokenId;


    /**
     * testName, nickname
     */
    private static Stream<Arguments> validParameter() {
        return Stream.of(
                Arguments.of("[한글] 닉네임 변경 테스트", "네임"),
                Arguments.of("[한글 - 양끝 공백포함(trim 테스트)] 닉네임 변경 테스트", "  닉네임 "),
                Arguments.of("[한글 - 앞쪽 공백포함(trim 테스트)] 닉네임 변경 테스트", "  닉네임"),
                Arguments.of("[한글 - 뒤쪽 공백포함(trim 테스트)] 닉네임 변경 테스트", "닉네임  "),
                Arguments.of("[영어] 닉네임 변경 테스트", "HIhiHi"),
                Arguments.of("[한,영,숫자] 닉네임 변경 테스트", "김hi123"),
                Arguments.of("[공백 1개] 닉네임 변경 테스트", "김 범중"),
                Arguments.of("[공백 1개] 닉네임 변경 테스트", "김 범 중")
        );
    }

    /**
     * testName, nickname
     */
    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("[숫자 단독 사용] 닉네임 변경 테스트", "123123"),
                Arguments.of("[숫자 단독 사용] 닉네임 변경 테스트", "12"),
                Arguments.of("[공백 2개] 닉네임 변경 테스트", "닉  네임"),
                Arguments.of("[특수문자] 닉네임 변경 테스트", "김!범!중!"),
                Arguments.of("[올바르지 않은 한글] 닉네임 변경 테스트", "김ㅁㄴㅇ"),
                Arguments.of("[empty] 닉네임 변경 테스트", ""),
                Arguments.of("[blank] 닉네임 변경 테스트", "   "),
                Arguments.of("[null] 닉네임 변경 테스트", null),
                Arguments.of("[2자 미만 한글] 닉네임 변경 테스트", "김"),
                Arguments.of("[2자 미만 영어] 닉네임 변경 테스트", "a"),
                Arguments.of("[12자 초과 한글] 닉네임 변경 테스트", "가나다라마바사아자차카타파하"),
                Arguments.of("[12자 초과 영어] 닉네임 변경 테스트", "abcdefghijklmnopqrstuvwxyz")
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
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // mockMvc에서 @AuthenticaionPrincipal CustomOAuth2User를 사용하기 위한 세팅
    @BeforeEach
    void OAuth2UserSetup() {
        CustomOAuth2User customOAuth2User = customOAuth2UserService.handleNewUser("authId", "authToken", "picture");
        SecurityTestUtil.setUpSecurityContext(customOAuth2User);
        testUser = userRepository.findByAuthIdAndActivated("authId", true)
                .orElseThrow(() -> new RuntimeException("테스트를 위한 유저 생성 실패"));

        String value = jwtUtil.createRefreshToken(testUser.getId(), testUser.getNickname(), testUser.getPicture(),
                testUser.getRole().name());
        refreshTokenService.addRefreshToken(testUser, value);

        originalRefreshTokenId = testUser.getRefreshToken().getId();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("validParameter")
    @DisplayName("[OK] changeNickname : 닉네임 변경")
    void 올바른_updateNickname_테스트(String testName, String nickname) throws Exception {
        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/nicknames";
        final NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest(nickname);
        final String requestBody = objectMapper.writeValueAsString(nicknameUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());

        // given
        // 기존 refreshtoken이 revoke되었는지?
        RefreshToken refreshToken = refreshTokenRepository.findById(originalRefreshTokenId).get();
        assertTrue(refreshToken.getRevoked());

        // 쿠키에 새로운 refreshtoken이 들어갔는지?
        Cookie refreshTokenCookie = result.andReturn().getResponse().getCookie(REFRESH.getType());

        assertThat(refreshTokenCookie).isNotNull();
        assertEquals(jwtUtil.getNickname(refreshTokenCookie.getValue()), nickname.trim());

        String accessTokenHeader = result.andReturn().getResponse().getHeader(ACCESS.getType());

        assertThat(accessTokenHeader).isNotNull();
        assertEquals(jwtUtil.getNickname(accessTokenHeader.substring(7)), nickname.trim());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidParameter")
    @DisplayName("[Bad Request] changeNickname : 닉네임 변경 - 올바르지 않은 규칙")
    void 올바르지않은_규칙_updateNickname_테스트(String testName, String nickname) throws Exception {
        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/nicknames";
        final NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest(nickname);
        final String requestBody = objectMapper.writeValueAsString(nicknameUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("[Bad Request] changeNickname : 닉네임 변경 - 유저의 user_id가 일치하지 않는 경우")
    void userId_일치하지_않는경우_updateNickname_테스트() throws Exception {
        // given
        final String url = "/api/v1/users/" + 123123123 + "/nicknames";
        final NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest("nickname");
        final String requestBody = objectMapper.writeValueAsString(nicknameUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[Bad Request] changeNickname : 닉네임 변경 - 탈퇴한 유저의 경우")
    void 탈퇴한유저_updateNickname_테스트() throws Exception {
        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/nicknames";
        final NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest("nickname");
        final String requestBody = objectMapper.writeValueAsString(nicknameUpdateRequest);

        // 유저 비활성화
        testUser.deactivate();
        userRepository.save(testUser);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[Bad Request] changeNickname : 닉네임 변경 - 이전과 동일한 닉네임")
    void 이전과_동일한닉네임_updateNickname_테스트() throws Exception {
        // 첫 가입시 만들어준 닉네임이 "객기부리는 멋진 아기코끼리 100"이면 12자가 넘기때문에 12자가 넘어서 BadRequest.
        testUser.changeNickname("newname");
        userRepository.save(testUser);
        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/nicknames";
        final NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest(testUser.getNickname());
        final String requestBody = objectMapper.writeValueAsString(nicknameUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[Bad Request] changeNickname : 닉네임 변경 - 중복된 닉네임")
    void 중복닉네임_updateNickname_테스트() throws Exception {
        final String existNickname = "existname";

        User user = User.builder()
                .authId("authId")
                .nickname(existNickname)
                .build();

        userRepository.save(user);

        // given
        final String url = "/api/v1/users/" + testUser.getId() + "/nicknames";
        final NicknameUpdateRequest nicknameUpdateRequest = new NicknameUpdateRequest(existNickname);
        final String requestBody = objectMapper.writeValueAsString(nicknameUpdateRequest);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isConflict());
    }
}