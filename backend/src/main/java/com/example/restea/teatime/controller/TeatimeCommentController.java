package com.example.restea.teatime.controller;

import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.teatime.dto.TeatimeCommentCreationRequest;
import com.example.restea.teatime.dto.TeatimeCommentCreationResponse;
import com.example.restea.teatime.dto.TeatimeCommentDeleteResponse;
import com.example.restea.teatime.dto.TeatimeCommentViewResponse;
import com.example.restea.teatime.service.TeatimeCommentService;
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
@RequestMapping("/api/v1/teatimes/{teatimeBoardId}")
public class TeatimeCommentController {
    private final TeatimeCommentService teatimeCommentService;

    /**
     * 주어진 티타임 게시글 댓글 조회
     *
     * @param teatimeBoardId 티타임 게시글 ID.
     * @param perPage        페이지 당 항목 수.
     * @param page           페이지 번호.
     * @return 페이지 정보와 댓글 리스트를 포함하는 ResponseEntity 객체를 반환합니다. 댓글 조회에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @GetMapping("/comments")
    public ResponseEntity<ResponseDTO<?>> getTeatimeCommentList(
            @PathVariable("teatimeBoardId") Integer teatimeBoardId,
            @NotNull @Positive @RequestParam("perPage") Integer perPage,
            @NotNull @Positive @RequestParam("page") Integer page) {

        ResponseDTO<List<TeatimeCommentViewResponse>> result = teatimeCommentService.getTeatimeCommentList(
                teatimeBoardId, page, perPage);

        if (result.getData().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(result);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    /**
     * 주어진 티타임 게시글에 댓글 작성
     *
     * @param teatimeBoardId   티타임 게시글 ID.
     * @param request          content 댓글 내용.
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 작성한 댓글 정보를 포함하는 ResponseEntity 객체를 반환합니다. 댓글 작성에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @PostMapping("/comments")
    public ResponseEntity<ResponseDTO<?>> createTeatimeComment(
            @PathVariable("teatimeBoardId") Integer teatimeBoardId,
            @Valid @RequestBody TeatimeCommentCreationRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        TeatimeCommentCreationResponse result = teatimeCommentService.createTeatimeComment(request.getContent(),
                teatimeBoardId, customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.from(result));
    }

    /**
     * 주어진 티타임 게시글에 댓글 삭제(activate를 false로 변경)
     *
     * @param teatimeBoardId   티타임 게시글 ID
     * @param teatimeCommentId 티타임 댓글 ID
     * @param customOAuth2User 현재 인증된 사용자.
     * @return 삭제한 댓글 ID를 포함하는 ResponseEntity 객체를 반환합니다. 댓글 삭제에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @PatchMapping("/deactivated-comments/{teatimeCommentId}")
    public ResponseEntity<ResponseDTO<?>> deactivateShareComment(
            @PathVariable("teatimeBoardId") Integer teatimeBoardId,
            @PathVariable("teatimeCommentId") Integer teatimeCommentId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        TeatimeCommentDeleteResponse result
                = teatimeCommentService.deactivateTeatimeComment(teatimeBoardId, teatimeCommentId,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(result));
    }
}
