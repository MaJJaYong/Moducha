package com.example.restea.share.util;

import static com.example.restea.share.enums.ShareBoardMessage.SHARE_BOARD_NOT_FOUND;
import static com.example.restea.share.enums.ShareCommentMessage.SHARE_COMMENT_NOT_FOUND;
import static com.example.restea.share.enums.ShareReplyMessage.SHARE_REPLY_NOT_FOUND;
import static com.example.restea.user.enums.UserMessage.USER_ALREADY_WITHDRAWN;
import static com.example.restea.user.enums.UserMessage.USER_NOT_FOUND;

import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareComment;
import com.example.restea.share.entity.ShareParticipant;
import com.example.restea.share.entity.ShareReply;
import com.example.restea.share.enums.ShareParticipantMessage;
import com.example.restea.share.repository.ShareBoardRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ShareUtil {

    public static User getActivatedUser(UserRepository userRepository, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND.getMessage()));
        if (!user.getActivated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_ALREADY_WITHDRAWN.getMessage());
        }
        return user;
    }

//    public static ShareBoard getShareBoard(ShareBoardRepository shareBoardRepository, Integer shareBoardId) {
//        return shareBoardRepository.findById(shareBoardId)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND, SHARE_BOARD_NOT_FOUND.getMessage()));
//    }

    public static ShareBoard getActivatedShareBoard(ShareBoardRepository shareBoardRepository, Integer shareBoardId) {
        return shareBoardRepository.findByIdAndActivated(shareBoardId, true)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, SHARE_BOARD_NOT_FOUND.getMessage()));
    }

    public static ShareComment getShareComment(Integer shareCommentId, ShareBoard shareBoard) {
        return shareBoard.getShareComments().stream()
                .filter(comment -> Objects.equals(comment.getId(), shareCommentId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, SHARE_COMMENT_NOT_FOUND.getMessage()));
    }

    public static ShareComment getActivatedShareComment(Integer shareCommentId, ShareBoard shareBoard) {
        return shareBoard.getShareComments().stream()
                .filter(comment -> Objects.equals(comment.getId(), shareCommentId))
                .filter(ShareComment::getActivated)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, SHARE_COMMENT_NOT_FOUND.getMessage()));
    }

    public static ShareReply getActivatedShareReply(Integer shareReplyId, ShareComment shareComment) {
        return shareComment.getShareReplies().stream()
                .filter(reply -> Objects.equals(reply.getId(), shareReplyId))
                .filter(ShareReply::getActivated)
                .findFirst()
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, SHARE_REPLY_NOT_FOUND.getMessage()));
    }

    public static ShareParticipant getShareParticipant(User activatedUser, ShareBoard shareBoard) {
        return shareBoard.getShareParticipants().stream()
                .filter(participant -> Objects.equals(participant.getUser(), activatedUser))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ShareParticipantMessage.SHARE_PARTICIPANT_NOT_FOUND.getMessage()));
    }

}
