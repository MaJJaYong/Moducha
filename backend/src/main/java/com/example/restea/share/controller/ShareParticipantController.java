package com.example.restea.share.controller;

import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.share.dto.ShareCancelResponse;
import com.example.restea.share.dto.ShareJoinCheckResponse;
import com.example.restea.share.dto.ShareJoinListResponse;
import com.example.restea.share.dto.ShareJoinRequest;
import com.example.restea.share.dto.ShareJoinResponse;
import com.example.restea.share.service.ShareParticipantService;
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
@RequestMapping("/api/v1/shares/{share_board_id}/participants")
public class ShareParticipantController {

    private final ShareParticipantService service;

    @PostMapping
    public ResponseEntity<ResponseDTO<?>> participateShare(
            @PathVariable("share_board_id") Integer shareBoardId,
            @Valid @RequestBody ShareJoinRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        ShareJoinResponse data = service.participate(
                shareBoardId, request, customOAuth2User.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .data(data)
                        .build());
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<ResponseDTO<?>> isParticipatedShare(
            @PathVariable("share_board_id") Integer shareBoardId,
            @PathVariable("user_id") Integer userId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        ShareJoinCheckResponse data = service.isParticipated(shareBoardId, userId, customOAuth2User.getUserId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.builder()
                        .data(data)
                        .build());
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<ResponseDTO<?>> cancelShare(
            @PathVariable("share_board_id") Integer shareBoardId,
            @PathVariable("user_id") Integer targetId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        ShareCancelResponse data = service.cancel(shareBoardId, targetId, customOAuth2User.getUserId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.builder()
                        .data(data)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<?>> getShareParticipants(
            @PathVariable("share_board_id") Integer shareBoardId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        List<ShareJoinListResponse> data = service.getShareParticipants(
                shareBoardId, customOAuth2User.getUserId());
        HttpStatus status = data.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(ResponseDTO.builder()
                        .data(data)
                        .build());
    }
}
