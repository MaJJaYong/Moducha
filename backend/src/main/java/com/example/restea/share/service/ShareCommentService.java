package com.example.restea.share.service;

import static com.example.restea.share.enums.ShareBoardMessage.SHARE_BOARD_USER_NOT_ACTIVATED;
import static com.example.restea.share.enums.ShareCommentMessage.SHARE_COMMENT_NOT_WRITER;
import static com.example.restea.share.util.ShareUtil.getActivatedShareBoard;
import static com.example.restea.share.util.ShareUtil.getActivatedShareComment;
import static com.example.restea.share.util.ShareUtil.getActivatedUser;

import com.example.restea.common.dto.PaginationDTO;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.share.dto.ShareCommentCreationResponse;
import com.example.restea.share.dto.ShareCommentDeleteResponse;
import com.example.restea.share.dto.ShareCommentViewResponse;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareComment;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.share.repository.ShareCommentRepository;
import com.example.restea.share.repository.ShareReplyRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class ShareCommentService {
    private final ShareBoardRepository shareBoardRepository;
    private final ShareCommentRepository shareCommentRepository;
    private final UserRepository userRepository;
    private final ShareReplyRepository shareReplyRepository;

    public ResponseDTO<List<ShareCommentViewResponse>> getShareCommentList(Integer shareBoardId, Integer page,
                                                                           Integer perPage) {

        ShareBoard activatedShareBoard = getActivatedShareBoard(shareBoardRepository, shareBoardId);

        Page<ShareComment> shareComments = getShareComments(activatedShareBoard, page, perPage);
        List<ShareCommentViewResponse> data = createResponseFromShareComments(shareComments.getContent());

        PaginationDTO pagination = PaginationDTO.of((int) shareComments.getTotalElements(), page, perPage);

        return ResponseDTO.of(data, pagination);
    }

    @Transactional
    public ShareCommentCreationResponse createShareComment(String content, Integer shareBoardId, Integer userId) {

        User activatedUser = getActivatedUser(userRepository, userId);
        ShareBoard activatedShareBoard = getActivatedShareBoard(shareBoardRepository, shareBoardId);

        checkActivatedShareBoardWriter(activatedShareBoard);

        ShareComment shareComment = ShareComment.builder()
                .user(activatedUser)
                .content(content)
                .shareBoard(activatedShareBoard)
                .build();

        shareCommentRepository.save(shareComment);

        return ShareCommentCreationResponse.of(shareComment);
    }

    @Transactional
    public ShareCommentDeleteResponse deactivateShareComment(Integer shareBoardId, Integer shareCommentId,
                                                             Integer userId) {

        User activatedUser = getActivatedUser(userRepository, userId);
        ShareBoard activatedShareBoard = getActivatedShareBoard(shareBoardRepository, shareBoardId);
        ShareComment activatedComment = getActivatedShareComment(shareCommentId, activatedShareBoard);

        checkWriter(activatedComment, activatedUser);

        activatedComment.deactivate();

        return ShareCommentDeleteResponse.from(shareCommentId);
    }

    private void checkWriter(ShareComment shareComment, User activatedUser) {
        if (!Objects.equals(shareComment.getUser().getId(), activatedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, SHARE_COMMENT_NOT_WRITER.getMessage());
        }
    }

    private List<ShareCommentViewResponse> createResponseFromShareComments(List<ShareComment> shareComments) {
        List<ShareCommentViewResponse> data = new ArrayList<>();

        for (ShareComment shareComment : shareComments) {
            Integer replyCount = shareReplyRepository.countByShareComment(shareComment).intValue();
            data.add(ShareCommentViewResponse.of(shareComment, replyCount));
        }
        return data;
    }

    private Page<ShareComment> getShareComments(ShareBoard activatedShareBoard, Integer page,
                                                Integer perPage) {

        return shareCommentRepository.findAllByShareBoard(activatedShareBoard, PageRequest.of(page - 1, perPage));
    }

    private static void checkActivatedShareBoardWriter(ShareBoard activatedShareBoard) {
        if (activatedShareBoard.getUser().getActivated()) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                SHARE_BOARD_USER_NOT_ACTIVATED.getMessage());
    }
}
