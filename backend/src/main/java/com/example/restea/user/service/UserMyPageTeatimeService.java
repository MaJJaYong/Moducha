package com.example.restea.user.service;

import com.example.restea.common.dto.PaginationDTO;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.teatime.dto.TeatimeListResponse;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeParticipantRepository;
import com.example.restea.user.repository.ParticipatedTeatimeBoardRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
public class UserMyPageTeatimeService {

    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeParticipantRepository teatimeParticipantRepository;
    private final ParticipatedTeatimeBoardRepository participatedTeatimeBoardRepository;

    /**
     * 내가 작성한 TeatimeBoardList를 최신순으로 불러오는 메소드
     *
     * @param userId  유저Id
     * @param page    요청받은 페이지 Number
     * @param perPage 페이지 컨텐츠 개수
     * @return TeatimeListResponse의 List를 Data로 가지는 ResponseDTO
     */
    @Transactional
    public ResponseDTO<List<TeatimeListResponse>> getTeatimeBoardList(Integer userId, Integer page, Integer perPage) {

        Page<TeatimeBoard> teatimeBoards = fetchActiveTeatimeBoards(userId, page, perPage); // 아직 기간이 지나지 않고 활성화된 게시글
        List<TeatimeListResponse> data = createResponseFormTeatimeBoards(teatimeBoards.getContent());
        Long count = calculateCount();

        PaginationDTO pagination = PaginationDTO.of(count.intValue(), page, perPage);

        return ResponseDTO.of(data, pagination);
    }

    /**
     * 최신순 내림차순으로 티타임 글 조회
     *
     * @param page    페이지
     * @param perPage 페이지 당 컨텐츠 개수
     * @return 컨텐츠 Page 객체
     */
    private @NotNull Page<TeatimeBoard> fetchActiveTeatimeBoards(Integer userId, Integer page, Integer perPage) {

        PageRequest pageRequest = PageRequest.of(page - 1, perPage, Sort.by("createdDate").descending());
        Page<TeatimeBoard> teatimeBoards = teatimeBoardRepository.findAllByActivatedAndUserId(true, userId,
                pageRequest);

        if (teatimeBoards.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        return teatimeBoards;
    }

    /**
     * 내가 쓴 티타임 글 Page 객체 생성
     *
     * @param teatimeBoards 티타임 글
     * @return 티타임 글 Response List
     */
    private List<TeatimeListResponse> createResponseFormTeatimeBoards(List<TeatimeBoard> teatimeBoards) {
        List<TeatimeListResponse> data = new ArrayList<>();
        teatimeBoards.forEach(teatimeBoard -> {
            Integer participant = teatimeParticipantRepository.countByTeatimeBoard(teatimeBoard).intValue();
            data.add(TeatimeListResponse.of(teatimeBoard, participant));
        });
        return data;
    }

    /**
     * 총 개수 세기
     *
     * @return 총 개수
     */
    private Long calculateCount() {
        return teatimeBoardRepository.countByActivated(true);
    }

    @Transactional
    public ResponseDTO<List<TeatimeListResponse>> getParticipatedTeatimeBoardList(Integer userId, String sort,
                                                                                  Integer page, Integer perPage) {
        if (isInvalidSort(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort");
        }

        List<TeatimeBoard> teatimeBoards = fetchActiveParticipatedTeatimeBoards(userId, sort, page, perPage);

        // teatimeboard를 teatimelistresponse로
        List<TeatimeListResponse> data = createResponseFormTeatimeBoards(teatimeBoards);
        Long count = calculateParticipatedCount(userId, sort); // 조건에 해당하는 전체 teatimeboard의 개수

        PaginationDTO pagination = PaginationDTO.of(count.intValue(), page, perPage);

        return ResponseDTO.of(data, pagination);
    }

    /**
     * @param userId  userId
     * @param sort    정렬 정보
     * @param page    몇번쨰 페이지인지?
     * @param perPage 넘겨줄 데이터 개수
     * @return TeatimeBoard List
     */
    private @NotNull List<TeatimeBoard> fetchActiveParticipatedTeatimeBoards(Integer userId, String sort, Integer page,
                                                                             Integer perPage) {
        List<TeatimeBoard> teatimeBoards = participatedTeatimeBoardRepository.findParticipatedTeatimeBoardsBySort(
                userId, sort, page, perPage, true);

        if (teatimeBoards.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }
        return teatimeBoards;
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
     * @return 참여 teatimeboard 개수
     */
    private Long calculateParticipatedCount(Integer userId, String sort) {
        return participatedTeatimeBoardRepository.countParticipatedTeatimeBoardsBySort(userId, sort, true);
    }

    /**
     * 내가 작성하거나 참여한 티타임 리스트를 불러오는 메소드
     *
     * @param userId 사용자 id
     * @return 내가 작성하거나 참여한 티타임 리스트
     */
    @Transactional
    public List<TeatimeListResponse> getMyTeatimeList(Integer userId) {

        LocalDateTime timeOffsetMinutes = LocalDateTime.now().minusMinutes(30);
        Page<TeatimeBoard> teatimeBoards = teatimeBoardRepository.findMyTeatimeList(
                userId, timeOffsetMinutes, PageRequest.of(0, 12, Sort.by("broadcastDate").ascending()));
        return createResponseFormTeatimeBoards(teatimeBoards);
    }

    /**
     * 내가 쓴 티타임 글 Page 객체 생성
     *
     * @param teatimeBoards 티타임 글
     * @return 티타임 글 Response List
     */
    private List<TeatimeListResponse> createResponseFormTeatimeBoards(Page<TeatimeBoard> teatimeBoards) {
        List<TeatimeListResponse> data = new ArrayList<>();
        teatimeBoards.getContent().forEach(teatimeBoard -> {
            Integer participant = teatimeParticipantRepository.countByTeatimeBoard(teatimeBoard).intValue();
            data.add(TeatimeListResponse.of(teatimeBoard, participant));
        });
        return data;
    }
}
