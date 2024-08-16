package com.example.restea.share.service;

import static com.example.restea.share.enums.ShareParticipantMessage.SHARE_PARTICIPANT_AFTER_END_DATE;
import static com.example.restea.share.enums.ShareParticipantMessage.SHARE_PARTICIPANT_ALREADY_EXISTS;
import static com.example.restea.share.enums.ShareParticipantMessage.SHARE_PARTICIPANT_FORBIDDEN;
import static com.example.restea.share.enums.ShareParticipantMessage.SHARE_PARTICIPANT_FULL;
import static com.example.restea.share.enums.ShareParticipantMessage.SHARE_PARTICIPANT_USER_IS_WRITER;
import static com.example.restea.share.enums.ShareParticipantMessage.SHARE_PARTICIPANT_WRITER_DEACTIVATED;
import static com.example.restea.share.util.ShareUtil.getActivatedShareBoard;
import static com.example.restea.share.util.ShareUtil.getActivatedUser;
import static com.example.restea.share.util.ShareUtil.getShareParticipant;

import com.example.restea.share.dto.ShareCancelResponse;
import com.example.restea.share.dto.ShareJoinCheckResponse;
import com.example.restea.share.dto.ShareJoinListResponse;
import com.example.restea.share.dto.ShareJoinRequest;
import com.example.restea.share.dto.ShareJoinResponse;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareParticipant;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.share.repository.ShareParticipantRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ShareParticipantService {

    private final UserRepository userRepository;
    private final ShareBoardRepository shareBoardRepository;
    private final ShareParticipantRepository shareParticipantRepository;

    @Transactional
    public ShareJoinResponse participate(Integer shareBoardId, ShareJoinRequest request, Integer userId) {
        User activatedUser = getActivatedUser(userRepository, userId);
        ShareBoard activatedShareBoard = getActivatedShareBoard(shareBoardRepository, shareBoardId);
        checkWriterActivated(activatedShareBoard);
        if (checkUserIsWriter(activatedShareBoard, activatedUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SHARE_PARTICIPANT_USER_IS_WRITER.getMessage());
        }
        if (checkIfUserAlreadyParticipating(activatedShareBoard, activatedUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    SHARE_PARTICIPANT_ALREADY_EXISTS.getMessage());
        }
        checkMaxParticipants(activatedShareBoard);
        checkEndDate(activatedShareBoard);

        ShareParticipant shareParticipant = shareParticipantRepository.save(ShareParticipant.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .shareBoard(activatedShareBoard)
                .user(activatedUser)
                .build());

        return ShareJoinResponse.of(shareParticipant);
    }

    @Transactional
    public ShareCancelResponse cancel(Integer shareBoardId, Integer targetId, Integer userId) {
        checkAuthorized(userId, targetId);
        User activatedUser = getActivatedUser(userRepository, targetId);
        ShareBoard activatedShareBoard = getActivatedShareBoard(shareBoardRepository, shareBoardId);
        checkEndDate(activatedShareBoard);

        if (checkUserIsWriter(activatedShareBoard, activatedUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SHARE_PARTICIPANT_USER_IS_WRITER.getMessage());
        }

        ShareParticipant shareParticipant = getShareParticipant(activatedUser, activatedShareBoard);
        activatedShareBoard.getShareParticipants().remove(shareParticipant);

        return ShareCancelResponse.of(shareBoardId, targetId);
    }

    public List<ShareJoinListResponse> getShareParticipants(Integer shareBoardId, Integer userId) { // check valid
        User activatedUser = getActivatedUser(userRepository, userId);
        ShareBoard activatedShareBoard = getActivatedShareBoard(shareBoardRepository, shareBoardId);
        if (!checkUserIsWriter(activatedShareBoard, activatedUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, SHARE_PARTICIPANT_FORBIDDEN.getMessage());
        }

        List<ShareParticipant> list = shareParticipantRepository.findAllByShareBoardId(shareBoardId);
        return createResponseFromShareParticipants(list);
    }

    public ShareJoinCheckResponse isParticipated(Integer shareBoardId, Integer targetId, Integer userId) {
        checkAuthorized(userId, targetId);
        User activatedUser = getActivatedUser(userRepository, targetId);
        ShareBoard activatedShareBoard = getActivatedShareBoard(shareBoardRepository, shareBoardId);
        if (checkUserIsWriter(activatedShareBoard, activatedUser)) {
            return ShareJoinCheckResponse.of(shareBoardId, targetId, true);
        }
        if (checkIfUserAlreadyParticipating(activatedShareBoard, activatedUser)) {
            return ShareJoinCheckResponse.of(shareBoardId, targetId, true);
        }
        return ShareJoinCheckResponse.of(shareBoardId, targetId, false);
    }


    private boolean checkIfUserAlreadyParticipating(ShareBoard activatedShareBoard, User activatedUser) {
        return activatedShareBoard.getShareParticipants().stream()
                .anyMatch(participant -> participant.getUser().equals(activatedUser));
    }

    private boolean checkUserIsWriter(ShareBoard activatedShareBoard, User activatedUser) {
        return activatedShareBoard.getUser().getId().equals(activatedUser.getId());
    }

    private void checkMaxParticipants(ShareBoard activatedShareBoard) {
        if (activatedShareBoard.getShareParticipants().size() >= activatedShareBoard.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SHARE_PARTICIPANT_FULL.getMessage());
        }
    }

    private void checkAuthorized(Integer userId, Integer targetId) {
        if (!Objects.equals(userId, targetId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, SHARE_PARTICIPANT_FORBIDDEN.getMessage());
        }
    }

    private List<ShareJoinListResponse> createResponseFromShareParticipants(List<ShareParticipant> list) {
        List<ShareJoinListResponse> data = new ArrayList<>();
        list.forEach(shareParticipant -> data.add(ShareJoinListResponse.of(shareParticipant)));
        return data;
    }

    private void checkEndDate(ShareBoard activatedShareBoard) {
        if (activatedShareBoard.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, SHARE_PARTICIPANT_AFTER_END_DATE.getMessage());
        }
    }

    private void checkWriterActivated(ShareBoard activatedShareBoard) {
        if (!activatedShareBoard.getUser().getActivated()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    SHARE_PARTICIPANT_WRITER_DEACTIVATED.getMessage());
        }
    }

}


