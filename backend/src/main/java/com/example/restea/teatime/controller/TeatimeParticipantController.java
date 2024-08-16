package com.example.restea.teatime.controller;

import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.teatime.dto.TeatimeCancelResponse;
import com.example.restea.teatime.dto.TeatimeJoinCheckResponse;
import com.example.restea.teatime.dto.TeatimeJoinListResponse;
import com.example.restea.teatime.dto.TeatimeJoinRequest;
import com.example.restea.teatime.dto.TeatimeJoinResponse;
import com.example.restea.teatime.service.TeatimeParticipantService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/teatimes/{teatimeBoardId}/participants")
public class TeatimeParticipantController {

    private final TeatimeParticipantService teatimeParticipantService;

    /**
     * 주어진 티타임 게시글에 참가 신청
     *
     * @param teatimeBoardId   티타임 게시글 ID.
     * @param request          name, phone, address.
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 작성한 참가 정보를 포함하는 ResponseEntity 객체를 반환합니다. 참가 신청에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @PostMapping
    public ResponseEntity<?> addParticipant(@PathVariable("teatimeBoardId") Integer teatimeBoardId,
                                            @Valid @RequestBody TeatimeJoinRequest request,
                                            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        TeatimeJoinResponse result = teatimeParticipantService.addParticipant(teatimeBoardId, request,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.from(result));
    }

    /**
     * 주어진 티타임 게시글에 참가 취소
     *
     * @param teatimeBoardId   티타임 게시글 ID
     * @param userId           참가 취소할 사용자 ID
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 티타임 게시글 ID, 참가 취소하는 사용자 ID 포함하는 ResponseEntity 객체를 반환합니다. 참가 취소에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> cancelParticipant(@PathVariable("teatimeBoardId") Integer teatimeBoardId,
                                               @PathVariable("userId") Integer userId,
                                               @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        TeatimeCancelResponse result = teatimeParticipantService.cancelParticipation(teatimeBoardId, userId,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(result));
    }

    /**
     * 주어진 티타임 게시글에 참가 여부 조회
     *
     * @param teatimeBoardId   티타임 게시글 ID
     * @param userId           참가 여부 조회할 사용자 ID
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 티타임 게시글 ID, 참가 여부 조회할 사용자 ID, 참가 여부를 포함하는 ResponseEntity 객체를 반환합니다. 참가 여부 조회에 실패하면 에러 코드를 담은
     * ResponseEntity를 반환합니다.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> joinCheckParticipant(@PathVariable("teatimeBoardId") Integer teatimeBoardId,
                                                  @PathVariable("userId") Integer userId,
                                                  @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        TeatimeJoinCheckResponse result = teatimeParticipantService.joinCheckParticipant(teatimeBoardId, userId,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(result));
    }

    /**
     * 주어진 티타임 게시글에 참가자 목록 조회
     *
     * @param teatimeBoardId   티타임 게시글 ID
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 주어진 티타임 게시글에 참가하는 참가자 정보를 포함하는 ResponseEntity 객체를 반환합니다. 티타임 참가자 목록 조회에 실패하면 에러 코드를 담은 ResponseEntity를
     * 반환합니다.
     */
    @GetMapping
    public ResponseEntity<?> getTeatimeJoinList(@PathVariable("teatimeBoardId") Integer teatimeBoardId,
                                                @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        List<TeatimeJoinListResponse> result = teatimeParticipantService.getParticipants(teatimeBoardId,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(result));
    }
}
