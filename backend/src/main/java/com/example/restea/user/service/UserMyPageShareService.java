package com.example.restea.user.service;

import com.example.restea.common.dto.PaginationDTO;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.share.dto.ShareListResponse;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.share.repository.ShareParticipantRepository;
import com.example.restea.user.repository.ParticipatedShareBoardRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserMyPageShareService {

    private final ShareBoardRepository shareBoardRepository;
    private final ShareParticipantRepository shareParticipantRepository;
    private final ParticipatedShareBoardRepository participatedShareBoardRepository;

    /**
     * 내가 작성한 ShareBoardList를 최신순으로 불러오는 메소드
     *
     * @param userId  유저Id
     * @param page    요청받은 페이지 Number
     * @param perPage 페이지 컨텐츠 개수
     * @return ShareListResponse의 List를 Data로 가지는 ResponseDTO
     */
    @Transactional
    public ResponseDTO<List<ShareListResponse>> getShareBoardList(Integer userId, Integer page, Integer perPage) {

        Page<ShareBoard> shareBoards = fetchActiveShareBoards(userId, page, perPage); // 아직 기간이 지나지 않고 활성화된 게시글
        List<ShareListResponse> data = createResponseFormShareBoards(shareBoards.getContent());
        Long count = calculateCount(userId);

        PaginationDTO pagination = PaginationDTO.of(count.intValue(), page, perPage);

        return ResponseDTO.of(data, pagination);
    }

    /**
     * 최신순 내림차순으로 나눔 글 조회
     *
     * @param page    페이지
     * @param perPage 페이지 당 컨텐츠 개수
     * @return 컨텐츠 Page 객체
     */
    private @NotNull Page<ShareBoard> fetchActiveShareBoards(Integer userId, Integer page, Integer perPage) {

        PageRequest pageRequest = PageRequest.of(page - 1, perPage, Sort.by("createdDate").descending());
        Page<ShareBoard> shareBoards = shareBoardRepository.findAllByActivatedAndUserId(true, userId, pageRequest);

        if (shareBoards.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        return shareBoards;
    }

    /**
     * 내가 쓴 나눔 글 Page 객체 생성
     *
     * @param shareBoards 나눔 글
     * @return 나눔 글 Response List
     */
    private List<ShareListResponse> createResponseFormShareBoards(List<ShareBoard> shareBoards) {
        List<ShareListResponse> data = new ArrayList<>();
        shareBoards.forEach(shareBoard -> {
            Integer participant = shareParticipantRepository.countByShareBoard(shareBoard).intValue();
            data.add(ShareListResponse.of(shareBoard, participant));
        });
        return data;
    }

    /**
     * 총 개수 세기
     *
     * @return 총 개수
     */
    private Long calculateCount(Integer userId) {
        return shareBoardRepository.countByActivatedAndUserId(true, userId);
    }

    /**
     * @param userId  userId
     * @param sort    정렬 정보
     * @param page    몇번째 페이지인지?
     * @param perPage 넘겨줄 데이터 개수
     * @return ShareListResponse List의 ResponseDTO
     */
    @Transactional
    public ResponseDTO<List<ShareListResponse>> getParticipatedShareBoardList(Integer userId, String sort, Integer page,
                                                                              Integer perPage) {

        if (isInvalidSort(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort");
        }

        List<ShareBoard> shareBoards = fetchActiveParticipatedShareBoards(userId, sort, page, perPage);

        List<ShareListResponse> data = createResponseFormShareBoards(shareBoards); // shareboard를 sharelistresponse로
        Long count = calculateParticipatedCount(userId, sort); // 조건에 해당하는 전체 shareboard의 개수

        PaginationDTO pagination = PaginationDTO.of(count.intValue(), page, perPage);

        return ResponseDTO.of(data, pagination);
    }

    /**
     * @param userId  userId
     * @param sort    정렬 정보
     * @param page    몇번쨰 페이지인지?
     * @param perPage 넘겨줄 데이터 개수
     * @return ShareBoard List
     */
    private @NotNull List<ShareBoard> fetchActiveParticipatedShareBoards(Integer userId, String sort, Integer page,
                                                                         Integer perPage) {
        List<ShareBoard> shareBoards = participatedShareBoardRepository.findParticipatedShareBoardsBySort(
                userId, sort, page, perPage, true);

        if (shareBoards.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }
        return shareBoards;
    }

    /**
     * ongoing, before이 아닐 경우 true 반환
     *
     * @param sort 정렬 정보
     * @return boolean
     */
    private boolean isInvalidSort(String sort) {
        return !"ongoing".equals(sort) && !"before".equals(sort);
    }

    /**
     * @param userId userId
     * @param sort   정렬 정보
     * @return 참여 shareboard 개수
     */
    private Long calculateParticipatedCount(Integer userId, String sort) {
        return participatedShareBoardRepository.countParticipatedShareBoardsBySort(userId, sort, true);
    }
}