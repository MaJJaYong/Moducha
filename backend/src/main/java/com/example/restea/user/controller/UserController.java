package com.example.restea.user.controller;

import static com.example.restea.oauth2.enums.TokenType.ACCESS;

import com.example.restea.common.dto.PaginationAndSortingDto;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.common.util.Trim;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.record.dto.RecordListResponse;
import com.example.restea.share.dto.ShareListResponse;
import com.example.restea.teatime.dto.TeatimeListResponse;
import com.example.restea.user.dto.NicknameUpdateRequest;
import com.example.restea.user.dto.NicknameUpdateResponse;
import com.example.restea.user.entity.User;
import com.example.restea.user.service.UserMyPageRecordService;
import com.example.restea.user.service.UserMyPageShareService;
import com.example.restea.user.service.UserMyPageTeatimeService;
import com.example.restea.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserMyPageShareService userMyPageShareService;
    private final UserMyPageTeatimeService userMyPageTeatimeService;
    private final UserMyPageRecordService userMyPageRecordService;

    /**
     * 유저를 비활성화하는 메소드. 회원이 탈퇴버튼을 누를 경우 작동
     *
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @return 성공 시 204 No Content
     */
    @PatchMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String authToken = userService.withdrawUser(customOAuth2User.getUserId());
        userService.revokeGoogleAuth(authToken);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 닉네임 변경 메소드.
     *
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @param userId           userId
     * @param dto              nickname이 담긴  RequestBody
     * @param response         HttpResponse
     * @return 성공 시 200 OK
     */
    @PatchMapping("/{user_Id}/nicknames")
    public ResponseEntity<ResponseDTO<NicknameUpdateResponse>> changeNickname(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable("user_Id") Integer userId,
            @Trim @Valid @RequestBody NicknameUpdateRequest dto,
            HttpServletResponse response) {

        // 1. User의 타당성 체크
        User user = validateUser(customOAuth2User, userId);

        // 2. 닉네임을 변경하고 토큰을 갱신
        String nickname = dto.getNickname();
        updateNicknameAndTokens(user, nickname, response);

        // 3. 응답 생성

        NicknameUpdateResponse result = NicknameUpdateResponse.from(nickname);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(result));
    }


    // 닉네임 변경 및 토큰 갱신을 별도 메소드로 분리
    private void updateNicknameAndTokens(User user, String newNickname, HttpServletResponse response) {
        userService.changeNickname(user, newNickname);

        String accessToken = userService.getAccessToken(user);
        response.setHeader(ACCESS.getType(), accessToken);

        Cookie refreshToken = userService.getRefreshToken(user);
        response.addCookie(refreshToken);
    }

    /**
     * 내가 작성한 ShareBoardList를 최신순으로 불러오는 API
     *
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @param dto              sort, page, perPage query Parameter
     * @return ResponseEntity
     */
    @GetMapping("/{user_id}/mypage/shares")
    public ResponseEntity<ResponseDTO<?>> getShareBoardList(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable("user_id") Integer userId,
            @Valid @ModelAttribute PaginationAndSortingDto dto) {

        validateUser(customOAuth2User, userId);

        ResponseDTO<List<ShareListResponse>> shareBoardList =
                userMyPageShareService.getShareBoardList(customOAuth2User.getUserId(),
                        dto.getPage(),
                        dto.getPerPage());

        return ResponseEntity.status(HttpStatus.OK)
                .body(shareBoardList);
    }

    /**
     * 내가 작성한 TeatimeBoardList를 최신순으로 불러오는 API
     *
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @param dto              sort, page, perPage query Parameter
     * @return ResponseEntity
     */
    @GetMapping("/{user_id}/mypage/teatimes")
    public ResponseEntity<ResponseDTO<?>> getTeatimeBoardList(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable("user_id") Integer userId,
            @Valid @ModelAttribute PaginationAndSortingDto dto) {

        validateUser(customOAuth2User, userId);

        ResponseDTO<List<TeatimeListResponse>> teatimeBoardList =
                userMyPageTeatimeService.getTeatimeBoardList(customOAuth2User.getUserId(),
                        dto.getPage(),
                        dto.getPerPage());

        return ResponseEntity.status(HttpStatus.OK)
                .body(teatimeBoardList);
    }

    /**
     * 내가 참여 신청한 ShareBoardList를 불러오는 API
     *
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @param dto              sort, page, perPage query Parameter
     * @return ResponseEntity
     */
    @GetMapping("/{user_id}/mypage/participated-shares")
    public ResponseEntity<ResponseDTO<?>> getParticipatedShareBoardList(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable("user_id") Integer userId,
            @Valid @ModelAttribute PaginationAndSortingDto dto) {

        validateUser(customOAuth2User, userId);

        ResponseDTO<List<ShareListResponse>> shareBoardList =
                userMyPageShareService.getParticipatedShareBoardList(customOAuth2User.getUserId(),
                        dto.getSort(),
                        dto.getPage(),
                        dto.getPerPage());

        return ResponseEntity.status(HttpStatus.OK)
                .body(shareBoardList);
    }

    /**
     * 내가 참여 신청한 TeatimeBoardList를 불러오는 API
     *
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @param dto              sort, page, perPage query Parameter
     * @return ResponseEntity
     */
    @GetMapping("/{user_id}/mypage/participated-teatimes")
    public ResponseEntity<ResponseDTO<?>> getParticipatedTeatimeBoardList(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable("user_id") Integer userId,
            @Valid @ModelAttribute PaginationAndSortingDto dto) {

        validateUser(customOAuth2User, userId);

        ResponseDTO<List<TeatimeListResponse>> teatimeBoardList =
                userMyPageTeatimeService.getParticipatedTeatimeBoardList(customOAuth2User.getUserId(),
                        dto.getSort(),
                        dto.getPage(),
                        dto.getPerPage());

        return ResponseEntity.status(HttpStatus.OK)
                .body(teatimeBoardList);
    }

    @GetMapping("/{user_id}/mypage/records")
    public ResponseEntity<ResponseDTO<?>> getRecords(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable("user_id") Integer userId,
            @Valid @ModelAttribute PaginationAndSortingDto dto) {

        validateUser(customOAuth2User, userId);

        ResponseDTO<List<RecordListResponse>> recordList =
                userMyPageRecordService.getRecordList(customOAuth2User.getUserId(),
                        dto.getPage(),
                        dto.getPerPage());

        return ResponseEntity.status(HttpStatus.OK)
                .body(recordList);
    }

    @GetMapping("/{user_id}/my-teatimes")
    public ResponseEntity<ResponseDTO<?>> getMyTeatimeList(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @PathVariable("user_id") Integer userId) {

        validateUser(customOAuth2User, userId);

        List<TeatimeListResponse> teatimeList =
                userMyPageTeatimeService.getMyTeatimeList(customOAuth2User.getUserId());

        HttpStatus status = teatimeList.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(ResponseDTO.builder()
                        .data(teatimeList)
                        .build());
    }

    // 사용자 검증을 별도 메소드로 분리
    private User validateUser(CustomOAuth2User customOAuth2User, Integer userId) {
        return userService.checkValidUser(customOAuth2User.getUserId(), userId);
    }
}
