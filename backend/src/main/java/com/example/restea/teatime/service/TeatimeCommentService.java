package com.example.restea.teatime.service;

import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_USER_NOT_ACTIVATED;
import static com.example.restea.teatime.enums.TeatimeCommentMessage.TEATIME_COMMENT_NOT_WRITER;
import static com.example.restea.teatime.util.TeatimeUtil.getActivatedTeatimeBoard;
import static com.example.restea.teatime.util.TeatimeUtil.getActivatedTeatimeComment;
import static com.example.restea.teatime.util.TeatimeUtil.getActivatedUser;

import com.example.restea.common.dto.PaginationDTO;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.teatime.dto.TeatimeCommentCreationResponse;
import com.example.restea.teatime.dto.TeatimeCommentDeleteResponse;
import com.example.restea.teatime.dto.TeatimeCommentViewResponse;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeCommentRepository;
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
public class TeatimeCommentService {
    private final UserRepository userRepository;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeCommentRepository teatimeCommentRepository;
    private final TeatimeReplyRepository teatimeReplyRepository;

    public ResponseDTO<List<TeatimeCommentViewResponse>> getTeatimeCommentList(Integer teatimeBoardId, Integer page,
                                                                               Integer perPage) {

        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);

        Page<TeatimeComment> teatimeComments = getTeatimeComments(activatedTeatimeBoard, page, perPage);
        List<TeatimeCommentViewResponse> data = createResponseFromTeatimeComments(teatimeComments.getContent());

        PaginationDTO pagination = PaginationDTO.of((int) teatimeComments.getTotalElements(), page, perPage);

        return ResponseDTO.of(data, pagination);
    }

    @Transactional
    public TeatimeCommentCreationResponse createTeatimeComment(String content, Integer teatimeBoardId, Integer userId) {

        User activatedUser = getActivatedUser(userRepository, userId);
        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);

        checkActivatedTeatimeBoardWriter(activatedTeatimeBoard);

        TeatimeComment teatimeComment = TeatimeComment.builder()
                .user(activatedUser)
                .content(content)
                .teatimeBoard(activatedTeatimeBoard)
                .build();

        teatimeCommentRepository.save(teatimeComment);

        return TeatimeCommentCreationResponse.of(teatimeComment);
    }

    @Transactional
    public TeatimeCommentDeleteResponse deactivateTeatimeComment(Integer teatimeBoardId, Integer teatimeCommentId,
                                                                 Integer userId) {

        User activatedUser = getActivatedUser(userRepository, userId);
        TeatimeBoard teatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);
        TeatimeComment teatimeComment = getActivatedTeatimeComment(teatimeCommentId, teatimeBoard);

        checkWriter(teatimeComment, activatedUser);

        teatimeComment.deactivate();

        return TeatimeCommentDeleteResponse.from(teatimeCommentId);
    }

    private Page<TeatimeComment> getTeatimeComments(TeatimeBoard teatimeBoard, Integer page, Integer perPage) {

        return teatimeCommentRepository.findAllByTeatimeBoard(teatimeBoard, PageRequest.of(page - 1, perPage));
    }

    private List<TeatimeCommentViewResponse> createResponseFromTeatimeComments(List<TeatimeComment> teatimeComments) {
        List<TeatimeCommentViewResponse> data = new ArrayList<>();
        for (TeatimeComment teatimeComment : teatimeComments) {
            Integer replyCount = teatimeReplyRepository.countByTeatimeComment(teatimeComment).intValue();
            data.add(TeatimeCommentViewResponse.of(teatimeComment, replyCount));
        }
        return data;
    }

    private void checkWriter(TeatimeComment teatimeComment, User activatedUser) {
        if (!Objects.equals(teatimeComment.getUser(), activatedUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_COMMENT_NOT_WRITER.getMessage());
        }
    }

    private static void checkActivatedTeatimeBoardWriter(TeatimeBoard activatedTeatimeBoard) {
        if (activatedTeatimeBoard.getUser().getActivated()) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEATIME_BOARD_USER_NOT_ACTIVATED.getMessage());
    }

}
