package com.example.restea.oauth2.service;

import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.oauth2.dto.GoogleResponse;
import com.example.restea.oauth2.dto.OAuth2JwtMemberDTO;
import com.example.restea.oauth2.dto.OAuth2Response;
import com.example.restea.oauth2.entity.AuthToken;
import com.example.restea.oauth2.repository.AuthTokenRepository;
import com.example.restea.oauth2.util.AuthIdCreator;
import com.example.restea.oauth2.util.NicknameCreator;
import com.example.restea.user.entity.ROLE;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    //DefaultOAuth2UserService OAuth2UserService의 구현체

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String authValue = userRequest.getAccessToken().getTokenValue();

        // registrationId : 프로바이더 변수
        OAuth2Response oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());

        // DB 저장 구현
        // 전달받은 데이터에서 username으로 지칭할 수 있는 것이 없기에 별도의 메소드를 구현한다.
        String authId = AuthIdCreator.getAuthId(oAuth2Response);
        Optional<User> optionalUser = userRepository.findByAuthIdAndActivated(authId, true);

        String picture = oAuth2Response.getPicture();

        // 기존에 존재하는 유저는 handleExistingUser
        // 새로 가입한 유저는 handleNewUser
        return optionalUser.map(existUser -> handleExistingUser(existUser, picture))
                .orElseGet(() -> handleNewUser(authId, authValue, picture));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CustomOAuth2User handleNewUser(String authId, String authValue, String picture) {
        AuthToken authToken = createAuthToken(authValue); // AuthToken 생성
        User user = createNewUser(authId, authToken, picture); // User 생성

        userRepository.save(user); // User 저장
        authTokenRepository.save(authToken); // AuthToken 저장

        // CustomOAUth2User에 파라미터로 이용될 OAuth2JwtMemberDTO 생성
        OAuth2JwtMemberDTO oAuth2JwtMemberDTO = createOAuth2JwtMemberDTO(user);

        return new CustomOAuth2User(oAuth2JwtMemberDTO);
    }

    private User createNewUser(String authId, AuthToken authToken, String picture) {
        String nickname = getUniqueNickname();
        return User.builder()
                .nickname(nickname)
                .picture(picture)
                .authId(authId)
                .authToken(authToken)
                .build();
    }

    // DB에 닉네임이 존재하지 않을 때 까지 닉네임 생성
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public String getUniqueNickname() {
        String nickname = NicknameCreator.getNickname();
        while (userRepository.existsByNickname(nickname)) {
            nickname = NicknameCreator.getNickname();
        }
        return nickname;
    }

    private AuthToken createAuthToken(String authValue) {
        return AuthToken.builder()
                .value(authValue)
                .build();
    }

    private OAuth2JwtMemberDTO createOAuth2JwtMemberDTO(User user) {
        String role = Optional.ofNullable(user.getRole())
                .map(ROLE::name)
                .orElse(ROLE.USER.name());

        return OAuth2JwtMemberDTO.builder()
                .nickname(user.getNickname())
                .picture(user.getPicture())
                .userId(user.getId())
                .role(role)
                .build();
    }

    private CustomOAuth2User handleExistingUser(User existUser, String picture) {
        existUser.updatePicture(picture);
        userRepository.save(existUser);

        OAuth2JwtMemberDTO oAuth2JwtMemberDTO = OAuth2JwtMemberDTO.builder()
                .nickname(existUser.getNickname())
                .picture(existUser.getPicture())
                .userId(existUser.getId())
                .role(existUser.getRole().name())
                .build();

        return new CustomOAuth2User(oAuth2JwtMemberDTO);
    }
}