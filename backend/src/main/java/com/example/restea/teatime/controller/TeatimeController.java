package com.example.restea.teatime.controller;

import com.example.restea.common.dto.PaginationAndSearchDto;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.s3.service.S3ServiceImpl;
import com.example.restea.teatime.dto.TeatimeCreationRequest;
import com.example.restea.teatime.dto.TeatimeCreationResponse;
import com.example.restea.teatime.dto.TeatimeDeleteResponse;
import com.example.restea.teatime.dto.TeatimeListResponse;
import com.example.restea.teatime.dto.TeatimeUpdateRequest;
import com.example.restea.teatime.dto.TeatimeUpdateResponse;
import com.example.restea.teatime.dto.TeatimeViewResponse;
import com.example.restea.teatime.service.TeatimeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/teatimes")
public class TeatimeController {
    private final TeatimeService teatimeService;
    private final S3ServiceImpl s3ServiceImpl;

    /**
     * 티타임 게시글 목록 조회
     *
     * @param dto sort, page, perpage, searchBy, keyword.
     * @return 페이지 번호에 맞는 게시글 수 만큼 티타임 게시글 목록을 포함하는 ResponseEntity 객체를 반환합니다. 티타임 게시글 목록 조회에 실패하면 에러 코드를 담은
     * ResponseEntity를 반환합니다.
     */
    @GetMapping
    public ResponseEntity<ResponseDTO<?>> getTeatimeBoardList(@Valid @ModelAttribute PaginationAndSearchDto dto) {

        ResponseDTO<List<TeatimeListResponse>> result = teatimeService.getTeatimeBoardList(
                dto.getSort(),
                dto.getPage(),
                dto.getPerPage(),
                dto.getSearchBy(),
                dto.getKeyword());

        if (result.getData().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(result);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * 티타임 게시글 작성
     *
     * @param request          title, content, endDate, broadcastDate, maxParticipants
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @return 작성한 글 정보를 포함하는 ResponseEntity 객체를 반환합니다. 티타임 게시글 작성에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<?>> createTeatimeBoard(
            @Valid @RequestBody TeatimeCreationRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        s3ServiceImpl.deleteImagesNotUsedInContent(request);
        TeatimeCreationResponse result = teatimeService.createTeatimeBoard(request, customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.from(result));
    }

    /**
     * 주어진 티타임 게시글 조회
     *
     * @param teatimeBoardId 티타임게시판 ID
     * @return 티타임 게시글 정보를 포함하는 ResponseEntity 객체를 반환합니다. 티타임 게시글 조회에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @GetMapping("/{teatimeBoardId}")
    public ResponseEntity<ResponseDTO<?>> getTeatimeBoard(
            @PathVariable("teatimeBoardId") Integer teatimeBoardId) {

        TeatimeViewResponse result = teatimeService.getTeatimeBoard(teatimeBoardId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(result));
    }

    /**
     * 주어진 티타임 게시글 수정
     *
     * @param teatimeBoardId   티타임게시판 ID
     * @param request          title, content, endDate, broadcastDate, maxParticipants
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @return 수정한 티타임 게시글 정보를 포함하는 ResponseEntity 객체를 반환합니다. 티타임 게시글 수정에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @PatchMapping("/{teatimeBoardId}")
    public ResponseEntity<ResponseDTO<?>> updateTeatimeBoard(
            @PathVariable("teatimeBoardId") Integer teatimeBoardId,
            @Valid @RequestBody TeatimeUpdateRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        TeatimeUpdateResponse teatimeUpdateResponse = updateAndHandleImages(teatimeBoardId, request, customOAuth2User);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(teatimeUpdateResponse));
    }

    private @NotNull TeatimeUpdateResponse updateAndHandleImages(Integer teatimeBoardId, TeatimeUpdateRequest request,
                                                                 CustomOAuth2User customOAuth2User) {
        // 수정 전 Content
        String contentBefore = teatimeService.getOnlyTeatimeBoard(teatimeBoardId).getContent();

        TeatimeUpdateResponse teatimeUpdateResponse = teatimeService.updateTeatimeBoard(teatimeBoardId, request,
                customOAuth2User.getUserId());

        // 수정 후 Content
        String contentAfter = teatimeUpdateResponse.getContent();

        // 이미지 삭제 요청
        s3ServiceImpl.deleteImagesNotUsedInContent(request, contentBefore, contentAfter);
        return teatimeUpdateResponse;
    }

    /**
     * 주어진 티타임 게시글 삭제(activate를 false로 변경)
     *
     * @param teatimeBoardId   티타임게시판 ID
     * @param customOAuth2User SecurityContextHolder에 등록된 인증된 유저
     * @return 삭제한 티타임 게시판 ID를 포함하는 ResponseEntity 객체를 반환합니다. 티타임 게시글 삭제에 실패하면 에러 코드를 담은 ResponseEntity를 반환합니다.
     */
    @PatchMapping("/deactivated-teatimes/{teatimeBoardId}")
    public ResponseEntity<ResponseDTO<?>> deactivateTeatimeBoard(
            @PathVariable("teatimeBoardId") Integer teatimeBoardId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        TeatimeDeleteResponse result = teatimeService.deactivateTeatimeBoard(teatimeBoardId,
                customOAuth2User.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.from(result));
    }
}
