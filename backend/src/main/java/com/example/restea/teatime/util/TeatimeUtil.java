package com.example.restea.teatime.util;


import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_NOT_FOUND;
import static com.example.restea.teatime.enums.TeatimeCommentMessage.TEATIME_COMMENT_NOT_FOUND;
import static com.example.restea.user.enums.UserMessage.USER_NOT_ACTIVATED;
import static com.example.restea.user.enums.UserMessage.USER_NOT_FOUND;

import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TeatimeUtil {
    public static User getActivatedUser(UserRepository userRepository, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND.getMessage()));
        if (!user.getActivated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_ACTIVATED.getMessage());
        }
        return user;
    }

    public static TeatimeBoard getActivatedTeatimeBoard(TeatimeBoardRepository teatimeBoardRepository,
                                                        Integer teatimeBoardId) {
        return teatimeBoardRepository.findByIdAndActivated(teatimeBoardId, true)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, TEATIME_BOARD_NOT_FOUND.getMessage()));
    }

    public static TeatimeComment getActivatedTeatimeComment(Integer teatimeCommentId, TeatimeBoard teatimeBoard) {
        return teatimeBoard.getTeatimeComments().stream()
                .filter(comment -> Objects.equals(comment.getId(), teatimeCommentId))
                .filter(TeatimeComment::getActivated)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, TEATIME_COMMENT_NOT_FOUND.getMessage()));
    }
}
