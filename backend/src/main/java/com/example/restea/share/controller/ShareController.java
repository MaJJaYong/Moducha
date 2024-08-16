package com.example.restea.share.controller;

import com.example.restea.common.dto.PaginationAndSearchDto;
import com.example.restea.common.dto.PaginationDTO;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.oauth2.dto.CustomOAuth2User;
import com.example.restea.s3.service.S3ServiceImpl;
import com.example.restea.share.dto.ShareCreationRequest;
import com.example.restea.share.dto.ShareListResponse;
import com.example.restea.share.dto.ShareUpdateRequest;
import com.example.restea.share.dto.ShareUpdateResponse;
import com.example.restea.share.service.ShareService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/v1/shares")
public class ShareController {
    private final ShareService shareService;
    private final S3ServiceImpl s3ServiceImpl;

    @GetMapping
    public ResponseEntity<ResponseDTO<?>> getShareBoardList(
            @Valid @ModelAttribute PaginationAndSearchDto dto) {

        // data, pagination
        Map<String, Object> result = shareService.getShareBoardList(
                dto.getSort(),
                dto.getPage(),
                dto.getPerPage(),
                dto.getSearchBy(),
                dto.getKeyword());

        HttpStatus status =
                ((List<ShareListResponse>) result.get("data")).isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(ResponseDTO.builder()
                        .data(result.get("data"))
                        .pagination((PaginationDTO) result.get("pagination"))
                        .build());
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<?>> createShareBoard(
            @Valid @RequestBody ShareCreationRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        s3ServiceImpl.deleteImagesNotUsedInContent(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .data(shareService.createShareBoard(request, customOAuth2User.getUserId()))
                        .build());
    }

    @GetMapping("/{shareBoardId}")
    public ResponseEntity<ResponseDTO<?>> getShareBoard(
            @PathVariable("shareBoardId") Integer shareBoardId) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.builder()
                        .data(shareService.getShareBoard(shareBoardId))
                        .build());
    }

    @PatchMapping("/{shareBoardId}")
    public ResponseEntity<ResponseDTO<?>> updateShareBoard(
            @PathVariable("shareBoardId") Integer shareBoardId,
            @Valid @RequestBody ShareUpdateRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        ShareUpdateResponse shareUpdateResponse = updateAndHandleImages(shareBoardId, request, customOAuth2User);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.builder()
                        .data(shareUpdateResponse)
                        .build());
    }

    private @NotNull ShareUpdateResponse updateAndHandleImages(Integer shareBoardId, ShareUpdateRequest request,
                                                               CustomOAuth2User customOAuth2User) {
        // 수정 전 Content
        String contentBefore = shareService.getOnlyShareBoard(shareBoardId).getContent();

        ShareUpdateResponse shareUpdateResponse = shareService.updateShareBoard(shareBoardId, request,
                customOAuth2User.getUserId());

        // 수정 후 Content
        String contentAfter = shareUpdateResponse.getContent();

        // 이미지 삭제 요청
        s3ServiceImpl.deleteImagesNotUsedInContent(request, contentBefore, contentAfter);
        return shareUpdateResponse;
    }

    @PatchMapping("/deactivated-shares/{shareBoardId}")
    public ResponseEntity<ResponseDTO<?>> deactivateShareBoard(
            @PathVariable("shareBoardId") Integer shareBoardId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.builder()
                        .data(shareService.deactivateShareBoard(shareBoardId, customOAuth2User.getUserId()))
                        .build());
    }

}
