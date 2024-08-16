package com.example.restea.teatime.service;

import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_USER_NOT_ACTIVATED;
import static com.example.restea.teatime.enums.TeatimeCommentMessage.TEATIME_COMMENT_NOT_FOUND;
import static com.example.restea.teatime.enums.TeatimeReplyMessage.TEATIME_REPLY_NOT_FOUND;
import static com.example.restea.teatime.enums.TeatimeReplyMessage.TEATIME_REPLY_NOT_WRITER;
import static com.example.restea.teatime.util.TeatimeUtil.getActivatedTeatimeBoard;
import static com.example.restea.teatime.util.TeatimeUtil.getActivatedTeatimeComment;
import static com.example.restea.teatime.util.TeatimeUtil.getActivatedUser;

import com.example.restea.common.dto.PaginationDTO;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.teatime.dto.TeatimeReplyCreationResponse;
import com.example.restea.teatime.dto.TeatimeReplyDeleteResponse;
import com.example.restea.teatime.dto.TeatimeReplyViewResponse;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.entity.TeatimeReply;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeReplyRepository;
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
public class TeatimeReplyService {

    private final UserRepository userRepository;
    private final TeatimeReplyRepository teatimeReplyRepository;
    private final TeatimeBoardRepository teatimeBoardRepository;

    public ResponseDTO<List<TeatimeReplyViewResponse>> getTeatimeReplyList(
            Integer teatimeBoardId, Integer teatimeCommentId, Integer page, Integer perPage) {

        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);
        TeatimeComment teatimeComment = getTeatimeComment(teatimeCommentId, activatedTeatimeBoard);

        Page<TeatimeReply> teatimeReplies = getTeatimeReplies(teatimeComment, page, perPage);
        List<TeatimeReplyViewResponse> data = createResponseFromTeatimeReplies(teatimeReplies.getContent());

        PaginationDTO pagination = PaginationDTO.of((int) teatimeReplies.getTotalElements(), page, perPage);

        return ResponseDTO.of(data, pagination);
    }

    @Transactional
    public TeatimeReplyCreationResponse createTeatimeReply(Integer teatimeBoardId, Integer teatimeCommentId,
                                                           String content, Integer userId) {

        User activatedUser = getActivatedUser(userRepository, userId);
        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);
        checkActivatedTeatimeBoardWriter(activatedTeatimeBoard);

        TeatimeComment activatedTeatimeComment = getActivatedTeatimeComment(teatimeCommentId, activatedTeatimeBoard);

        TeatimeReply teatimeReply = TeatimeReply.builder()
                .content(content)
                .teatimeComment(activatedTeatimeComment)
                .user(activatedUser)
                .build();

        teatimeReplyRepository.save(teatimeReply);

        return TeatimeReplyCreationResponse.of(teatimeReply, activatedUser.getExposedNickname());
    }

    @Transactional
    public TeatimeReplyDeleteResponse deactivateTeatimeReply(Integer teatimeBoardId, Integer teatimeCommentId,
                                                             Integer teatimeReplyId, Integer userId) {

        User activatedUser = getActivatedUser(userRepository, userId);
        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);
        TeatimeComment teatimeComment = getTeatimeComment(teatimeCommentId, activatedTeatimeBoard);

        TeatimeReply activatedTeatimeReply = getActivatedTeatimeReply(teatimeReplyId, teatimeComment);
        checkWriter(activatedTeatimeReply, activatedUser);

        activatedTeatimeReply.deactivate();

        return TeatimeReplyDeleteResponse.from(activatedTeatimeReply);
    }

    public TeatimeComment getTeatimeComment(Integer teatimeCommentId, TeatimeBoard activatedTeatimeBoard) {
        return activatedTeatimeBoard.getTeatimeComments().stream()
                .filter(comment -> Objects.equals(comment.getId(), teatimeCommentId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, TEATIME_COMMENT_NOT_FOUND.getMessage()));
    }

    private Page<TeatimeReply> getTeatimeReplies(TeatimeComment teatimeComment, Integer page, Integer perPage) {
        return teatimeReplyRepository.findAllByTeatimeComment(teatimeComment, PageRequest.of(page - 1, perPage));
    }

    private List<TeatimeReplyViewResponse> createResponseFromTeatimeReplies(List<TeatimeReply> teatimeReplies) {
        List<TeatimeReplyViewResponse> data = new ArrayList<>();
        for (TeatimeReply teatimeReply : teatimeReplies) {
            data.add(TeatimeReplyViewResponse.of(teatimeReply));
        }
        return data;
    }

    private static void checkActivatedTeatimeBoardWriter(TeatimeBoard activatedTeatimeBoard) {
        if (activatedTeatimeBoard.getUser().getActivated()) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEATIME_BOARD_USER_NOT_ACTIVATED.getMessage());
    }

    public static TeatimeReply getActivatedTeatimeReply(Integer teatimeReplyId, TeatimeComment teatimeComment) {
        return teatimeComment.getTeatimeReplies().stream()
                .filter(reply -> Objects.equals(reply.getId(), teatimeReplyId))
                .filter(TeatimeReply::getActivated)
                .findFirst()
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, TEATIME_REPLY_NOT_FOUND.getMessage()));
    }

    private void checkWriter(TeatimeReply teatimeReply, User activatedUser) {
        if (!Objects.equals(teatimeReply.getUser(), activatedUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_REPLY_NOT_WRITER.getMessage());
        }
    }
}
