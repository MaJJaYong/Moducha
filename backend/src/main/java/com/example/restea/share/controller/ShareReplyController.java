package com.example.restea.share.controller;

import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.share.dto.ShareReplyCreationRequest;
import com.example.restea.share.dto.ShareReplyCreationResponse;
import com.example.restea.share.dto.ShareReplyDeleteResponse;
import com.example.restea.share.dto.ShareReplyViewResponse;
import com.example.restea.share.service.ShareReplyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/shares/{shareBoardId}/comments/{shareCommentId}")
public class ShareReplyController {

    private final ShareReplyService shareReplyService;

    @GetMapping("/replies")
    public ResponseEntity<ResponseDTO<?>> getShareReplyList(
            @PathVariable("shareBoardId") Integer shareBoardId,
            @PathVariable("shareCommentId") Integer shareCommentId,
            @NotNull @Positive @RequestParam("perPage") Integer perPage,
            @NotNull @Positive @RequestParam("page") Integer page) {

        ResponseDTO<List<ShareReplyViewResponse>> result
                = shareReplyService.getShareReplyList(shareBoardId, shareCommentId, page, perPage);
        HttpStatus status = result.getData().isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(result);
    }

    @PostMapping("/replies")
    public ResponseEntity<ResponseDTO<?>> createShareReply(
            @PathVariable("shareBoardId") Integer shareBoardId,
            @PathVariable("shareCommentId") Integer shareCommentId,
            @Valid @RequestBody ShareReplyCreationRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        ShareReplyCreationResponse result
                = shareReplyService.createShareReply(shareBoardId, request.getContent(), shareCommentId,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.from(result));
    }

    @PatchMapping("/deactivated-replies/{shareReplyId}")
    public ResponseEntity<ResponseDTO<?>> deactivateShareReply(
            @PathVariable("shareBoardId") Integer shareBoardId,
            @PathVariable("shareCommentId") Integer shareCommentId,
            @PathVariable("shareReplyId") Integer shareReplyId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        ShareReplyDeleteResponse result =
                shareReplyService.deactivateShareReply(shareBoardId, shareCommentId, shareReplyId,
                        customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(result));
    }
}
