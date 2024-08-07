package com.example.restea.share.controller;

import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.share.dto.ShareCommentCreationRequest;
import com.example.restea.share.dto.ShareCommentCreationResponse;
import com.example.restea.share.dto.ShareCommentDeleteResponse;
import com.example.restea.share.dto.ShareCommentListResponse;
import com.example.restea.share.service.ShareCommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
@RequestMapping("/api/v1/shares/{shareBoardId}/comments")
public class ShareCommentController {
    private final ShareCommentService shareCommentService;

    /**
     * 주어진 나눔 게시글 댓글 조회
     *
     * @param shareBoardId 나눔 게시글 ID.
     * @param perPage      페이지 당 항목 수.
     * @param page         페이지 번호.
     * @return 페이지 정보와 댓글 리스트를 포함하는 ResponseEntity 객체를 반환합니다. 댓글 조회에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @GetMapping
    public ResponseEntity<ResponseDTO<?>> getShareCommentList(
            @PathVariable("shareBoardId") Integer shareBoardId,
            @NotNull @Positive @RequestParam("perPage") Integer perPage,
            @NotNull @Positive @RequestParam("page") Integer page) {

        ResponseDTO<List<ShareCommentListResponse>> result = shareCommentService.getShareCommentList(shareBoardId, page,
                perPage);

        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    /**
     * 주어진 나눔 게시글에 댓글 작성
     *
     * @param shareBoardId     나눔 게시글 ID.
     * @param request          content 댓글 내용.
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 작성한 댓글 정보를 포함하는 ResponseEntity 객체를 반환합니다. 댓글 작성에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<?>> createShareBoard(
            @PathVariable("shareBoardId") Integer shareBoardId,
            @Valid @RequestBody ShareCommentCreationRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        ShareCommentCreationResponse result = shareCommentService.createShareComment(request, shareBoardId,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.of(result));
    }

    /**
     * 주어진 나눔 게시글에 댓글 삭제(activate를 false로 변경)
     *
     * @param shareCommentId   나눔 게시판 댓글 ID.
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 삭제한 댓글 ID를 포함하는 ResponseEntity 객체를 반환합니다. 댓글 삭제에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @PatchMapping("/{shareCommentId}")
    public ResponseEntity<ResponseDTO<?>> deactivateShareBoard(
            @PathVariable("shareCommentId") Integer shareCommentId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        ShareCommentDeleteResponse result = shareCommentService.deactivateShareComment(shareCommentId,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.of(result));
    }
}
