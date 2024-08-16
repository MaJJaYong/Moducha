package com.example.restea.share.service;

import static com.example.restea.share.enums.ShareBoardMessage.SHARE_BOARD_INVALID_KEYWORD;
import static com.example.restea.share.enums.ShareBoardMessage.SHARE_BOARD_INVALID_SEARCH_BY;
import static com.example.restea.share.enums.ShareBoardMessage.SHARE_BOARD_INVALID_SORT;
import static com.example.restea.share.enums.ShareBoardMessage.SHARE_BOARD_LESS_THAN_CURRENT_PARTICIPANTS;
import static com.example.restea.share.enums.ShareBoardMessage.SHARE_BOARD_NOT_WRITER;
import static com.example.restea.share.util.ShareUtil.getActivatedShareBoard;

import com.example.restea.common.dto.PaginationDTO;
import com.example.restea.share.dto.ShareCreationRequest;
import com.example.restea.share.dto.ShareCreationResponse;
import com.example.restea.share.dto.ShareDeleteResponse;
import com.example.restea.share.dto.ShareListResponse;
import com.example.restea.share.dto.ShareUpdateRequest;
import com.example.restea.share.dto.ShareUpdateResponse;
import com.example.restea.share.dto.ShareViewResponse;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareReply;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.share.repository.ShareBoardSearchRepository;
import com.example.restea.share.repository.ShareParticipantRepository;
import com.example.restea.share.util.ShareUtil;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class ShareService {

    private final ShareBoardRepository shareBoardRepository;
    private final UserRepository userRepository;
    private final ShareParticipantRepository shareParticipantRepository;
    private final ShareBoardSearchRepository shareBoardSearchRepository;

    @Transactional
    public Map<String, Object> getShareBoardList(
            String sort, Integer page, Integer perPage, String searchBy, String keyword) {

        checkSort(sort);
        if (searchBy != null || keyword != null) {
            checkSearchBy(searchBy);
            checkKeyword(keyword);
        }

        // data
        List<ShareBoard> shareBoards = getActivatedShareBoards(sort, page, perPage, searchBy,
                keyword); // 아직 기간이 지나지 않고 활성화된 게시글
        List<ShareListResponse> data = createResponseFormShareBoards(shareBoards);
        int totalElements = shareBoardSearchRepository.countBySortAndKeyword(sort, searchBy, keyword);

        // pagination info
        PaginationDTO pagination = PaginationDTO.of(totalElements, page, perPage);

        return Map.of("data", data, "pagination", pagination);
    }

    @Transactional
    public ShareCreationResponse createShareBoard(ShareCreationRequest request, Integer userId) {

        User activatedUser = ShareUtil.getActivatedUser(userRepository, userId);
        ShareBoard result = shareBoardRepository.save(request.toEntity().addUser(activatedUser));
        return ShareCreationResponse.of(result);
    }

    @Transactional
    public ShareViewResponse getShareBoard(Integer shareBoardId) {

        ShareBoard activatedShareBoard = getOnlyShareBoard(shareBoardId);
        Integer participants = shareParticipantRepository.countByShareBoard(activatedShareBoard).intValue();

        activatedShareBoard.addViewCount();

        String picture = activatedShareBoard.getUser().getPicture(); // 프로필 사진 가져오기

        return ShareViewResponse.of(activatedShareBoard, participants, picture);
    }

    public ShareBoard getOnlyShareBoard(Integer shareBoardId) {
        return getActivatedShareBoard(shareBoardRepository, shareBoardId);
    }

    @Transactional
    public ShareUpdateResponse updateShareBoard(Integer shareBoardId, ShareUpdateRequest request, Integer userId) {

        ShareBoard activatedShareBoard = getOnlyShareBoard(shareBoardId);
        User activatedUser = ShareUtil.getActivatedUser(userRepository, userId);
        checkAuthorized(activatedShareBoard, activatedUser);
        checkLessThanCurrentParticipants(request, activatedShareBoard);

        activatedShareBoard.update(request.getTitle(), request.getContent(), request.getMaxParticipants(),
                request.getEndDate());
        Integer participants = shareParticipantRepository.countByShareBoard(activatedShareBoard).intValue();
        return ShareUpdateResponse.of(activatedShareBoard, participants);
    }

    @Transactional
    public ShareDeleteResponse deactivateShareBoard(Integer shareBoardId, Integer userId) {

        ShareBoard activatedShareBoard = getOnlyShareBoard(shareBoardId);
        User activatedUser = ShareUtil.getActivatedUser(userRepository, userId);
        checkAuthorized(activatedShareBoard, activatedUser);

        activatedShareBoard.deactivate();

        // TODO : shareBoard.deactivate() 안으로 이동
        activatedShareBoard.getShareComments().forEach((comment) -> {
            comment.getShareReplies().forEach(ShareReply::deactivate);
            comment.deactivate();
        });
        shareParticipantRepository.deleteAll(activatedShareBoard.getShareParticipants());

        return ShareDeleteResponse.builder()
                .boardId(shareBoardId)
                .build();
    }

    private void checkSort(String sort) {
        if (!List.of("urgent", "latest").contains(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SHARE_BOARD_INVALID_SORT.getMessage());
        }
    }

    private void checkSearchBy(String searchBy) {
        if (searchBy == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SHARE_BOARD_INVALID_SEARCH_BY.getMessage());
        }
        if (!List.of("title", "content", "writer").contains(searchBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SHARE_BOARD_INVALID_SEARCH_BY.getMessage());
        }
    }

    private void checkKeyword(String keyword) {
        if (keyword == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SHARE_BOARD_INVALID_KEYWORD.getMessage());
        }
    }

    private @NotNull List<ShareBoard> getActivatedShareBoards(
            String sort, Integer page, Integer perPage, String searchBy, String keyword) {

        return shareBoardSearchRepository.findAllBySortAndKeyword(
                sort, page, perPage, searchBy, keyword);
    }

    private List<ShareListResponse> createResponseFormShareBoards(List<ShareBoard> shareBoards) {
        List<ShareListResponse> data = new ArrayList<>();
        for (ShareBoard shareBoard : shareBoards) {
            Integer participant = shareParticipantRepository.countByShareBoard(shareBoard).intValue();
            data.add(ShareListResponse.of(shareBoard, participant));
        }
        return data;
    }

    private void checkAuthorized(ShareBoard shareBoard, User activatedUser) {
        if (!Objects.equals(shareBoard.getUser().getId(), activatedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, SHARE_BOARD_NOT_WRITER.getMessage());
        }
    }

    private void checkLessThanCurrentParticipants(ShareUpdateRequest request, ShareBoard shareBoard) {
        boolean isLessThanCurrentParticipants =
                request.getMaxParticipants() < shareParticipantRepository.countByShareBoard(shareBoard);
        if (isLessThanCurrentParticipants) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    SHARE_BOARD_LESS_THAN_CURRENT_PARTICIPANTS.getMessage());
        }
    }

}
