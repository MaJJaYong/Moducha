package com.example.restea.live.controller;

import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.live.dto.LiveIsOpenResponseDTO;
import com.example.restea.live.dto.LiveKickResponseDTO;
import com.example.restea.live.dto.LiveMuteRequestDTO;
import com.example.restea.live.dto.LiveMuteResponseDTO;
import com.example.restea.live.dto.LiveRoomResponseDTO;
import com.example.restea.live.service.LiveService;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teatimes/{teatimeBoardId}/lives")
@RequiredArgsConstructor
public class LiveController {

    private final LiveService liveService;

    /**
     * 주어진 티타임 게시글 방송 생성 여부 조회
     *
     * @param teatimeBoardId   티타임게시판 ID.
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 방송 개설 여부를 포함하는 ResponseEntity 객체를 반환합니다. 방송 개설 여부 조회에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @GetMapping
    public ResponseEntity<ResponseDTO<LiveIsOpenResponseDTO>> isLiveOpen(
            @PathVariable("teatimeBoardId") int teatimeBoardId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        LiveIsOpenResponseDTO result = LiveIsOpenResponseDTO.from(
                liveService.isLiveOpen(teatimeBoardId, customOAuth2User.getUserId()));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDTO.from(result));
    }

    /**
     * 주어진 티타임 게시글 방송 생성
     *
     * @param teatimeBoardId   티타임게시판 ID. 티타임게시판ID을 외래키로 가지는 live테이블에 새로운 레코드 생성. liveId는 사용자가 연결하려는 방의 이름
     * @param customOAuth2User 현재 인증된 사용자.
     * @return liveId와 user 정보로 만든 JWT token을 포함하는 ResponseEntity 객체를 반환합니다. token 생성에 실패하면 에러 코드를 담은 ResponseEntity를
     * 반환합니다.
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<LiveRoomResponseDTO>> createLive(
            @PathVariable("teatimeBoardId") int teatimeBoardId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        LiveRoomResponseDTO result = LiveRoomResponseDTO.from(
                liveService.createLive(teatimeBoardId, customOAuth2User.getUserId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDTO.from(result));
    }

    /**
     * 주어진 방 이름과 참가자에 대한 토큰을 생성
     *
     * @param teatimeBoardId   티타임게시판 ID. 티타임게시판ID을 외래키로 가지는 live테이블에 저장된 liveId를 사용. liveId는 사용자가 연결하려는 방의 이름
     * @param customOAuth2User 현재 인증된 사용자. 이 사용자는 티타임 방송에 참가하려는 참가자의 정보로 쓰인다.
     * @return teatimeId와 user 정보로 만든 JWT token을 포함하는 ResponseEntity 객체를 반환합니다. token 생성에 실패하면 에러 메시지를 담은
     * ResponseEntity를 반환합니다.
     */
    @PostMapping("/token")
    public ResponseEntity<ResponseDTO<LiveRoomResponseDTO>> liveJoin(
            @PathVariable("teatimeBoardId") int teatimeBoardId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        LiveRoomResponseDTO result = LiveRoomResponseDTO.from(
                liveService.liveJoin(teatimeBoardId, customOAuth2User.getUserId()));

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDTO.from(result));
    }

    /**
     * 주어진 참가자에 방송에서 강퇴
     *
     * @param teatimeBoardId   티타임게시판 ID. 티타임게시판ID을 외래키로 가지는 live테이블에 저장된 liveId를 사용. liveId는 사용자가 참여하고 있는 방의 이름
     * @param kickUserId       강퇴될 사용자.
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 내용이 빈 ResponseEntity 객체를 반환합니다. 강퇴에 실패하면 에러 메시지를 담은 ResponseEntity를 반환합니다.
     */
    @PostMapping("/kick")
    public ResponseEntity<ResponseDTO<LiveKickResponseDTO>> liveKick(
            @PathVariable("teatimeBoardId") int teatimeBoardId, @RequestBody Integer kickUserId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        liveService.liveKick(teatimeBoardId, kickUserId, customOAuth2User.getUserId());

        LiveKickResponseDTO result = LiveKickResponseDTO.from(kickUserId);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDTO.from(result));
    }

    /**
     * 주어진 참가자에 trackSid에 해당하는 track 방송에서 음소거
     *
     * @param teatimeBoardId 티타임게시판 ID. 티타임게시판ID을 외래키로 가지는 live테이블에 저장된 liveId를 사용. liveId는 사용자가 참여하고 있는 방의 이름
     * @param request        muteUserId       음소거될 사용자. trackSid         음소거될 trackSid. isMute         음소거여부.
     * @return 내용이 빈 ResponseEntity 객체를 반환합니다. 음소거에 실패하면 에러 메시지를 담은 ResponseEntity를 반환합니다.
     */
    @PostMapping("/mute")
    public ResponseEntity<ResponseDTO<LiveMuteResponseDTO>> liveMute(
            @PathVariable("teatimeBoardId") int teatimeBoardId, @Valid @RequestBody LiveMuteRequestDTO request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        liveService.liveMute(teatimeBoardId, request, customOAuth2User.getUserId());

        LiveMuteResponseDTO result = LiveMuteResponseDTO.of(request);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDTO.from(result));
    }
}
