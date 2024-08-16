package com.example.restea.teatime.service;

import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_NOT_WRITER;
import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_USER_NOT_ACTIVATED;
import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_WRITER;
import static com.example.restea.teatime.enums.TeatimeParticipantMessage.TEATIME_PARTICIPANT_AFTER_END_DATE;
import static com.example.restea.teatime.enums.TeatimeParticipantMessage.TEATIME_PARTICIPANT_ALREADY_EXISTS;
import static com.example.restea.teatime.enums.TeatimeParticipantMessage.TEATIME_PARTICIPANT_FULL;
import static com.example.restea.teatime.enums.TeatimeParticipantMessage.TEATIME_PARTICIPANT_NOT_FOUND;
import static com.example.restea.teatime.util.TeatimeUtil.getActivatedTeatimeBoard;
import static com.example.restea.teatime.util.TeatimeUtil.getActivatedUser;

import com.example.restea.teatime.dto.TeatimeCancelResponse;
import com.example.restea.teatime.dto.TeatimeJoinCheckResponse;
import com.example.restea.teatime.dto.TeatimeJoinListResponse;
import com.example.restea.teatime.dto.TeatimeJoinRequest;
import com.example.restea.teatime.dto.TeatimeJoinResponse;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeParticipant;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeParticipantRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import com.example.restea.user.service.UserService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class TeatimeParticipantService {

    private final TeatimeParticipantRepository teatimeParticipantRepository;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public TeatimeJoinResponse addParticipant(Integer teatimeBoardId, TeatimeJoinRequest request, Integer userId) {

        User activatedUser = getActivatedUser(userRepository, userId);
        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);

        validateParticipationRequest(activatedTeatimeBoard, activatedUser);

        TeatimeParticipant teatimeParticipant = TeatimeParticipant.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .teatimeBoard(activatedTeatimeBoard)
                .user(activatedUser)
                .build();

        teatimeParticipantRepository.save(teatimeParticipant);

        return TeatimeJoinResponse.of(teatimeParticipant);
    }

    @Transactional
    public TeatimeCancelResponse cancelParticipation(Integer teatimeBoardId, Integer userId,
                                                     Integer customOAuth2UserId) {

        User activatedUser = userService.checkValidUser(customOAuth2UserId, userId);
        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);

        validateCancelRequest(activatedTeatimeBoard, activatedUser);

        TeatimeParticipant participant = teatimeParticipantRepository.findByTeatimeBoardAndUser(activatedTeatimeBoard,
                        activatedUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        TEATIME_PARTICIPANT_NOT_FOUND.getMessage()));

        teatimeParticipantRepository.delete(participant);

        return TeatimeCancelResponse.of(teatimeBoardId, userId);
    }

    @Transactional
    public TeatimeJoinCheckResponse joinCheckParticipant(Integer teatimeBoardId, Integer userId,
                                                         Integer customOAuth2UserId) {

        User activatedUser = userService.checkValidUser(customOAuth2UserId, userId);
        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);

        if (Objects.equals(activatedTeatimeBoard.getUser(), activatedUser)) {
            return TeatimeJoinCheckResponse.of(teatimeBoardId, userId, true);
        }

        if (teatimeParticipantRepository.existsByTeatimeBoardAndUser(activatedTeatimeBoard, activatedUser)) {
            return TeatimeJoinCheckResponse.of(teatimeBoardId, userId, true);
        }

        return TeatimeJoinCheckResponse.of(teatimeBoardId, userId, false);
    }

    @Transactional
    public List<TeatimeJoinListResponse> getParticipants(Integer teatimeBoardId, Integer userId) {

        User activatedUser = getActivatedUser(userRepository, userId);
        TeatimeBoard activatedTeatimeBoard = getActivatedTeatimeBoard(teatimeBoardRepository, teatimeBoardId);

        if (!Objects.equals(activatedTeatimeBoard.getUser(), activatedUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_BOARD_NOT_WRITER.getMessage());
        }

        return teatimeParticipantRepository.findByTeatimeBoard(activatedTeatimeBoard)
                .stream()
                .map(TeatimeJoinListResponse::of)
                .toList();
    }

    private void validateParticipationRequest(TeatimeBoard activatedTeatimeBoard, User activatedUser) {
        if (!activatedTeatimeBoard.getUser().getActivated()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEATIME_BOARD_USER_NOT_ACTIVATED.getMessage());
        }

        if (Objects.equals(activatedTeatimeBoard.getUser().getId(), activatedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_BOARD_WRITER.getMessage());
        }

        validateEndDate(activatedTeatimeBoard.getEndDate());

        if (activatedTeatimeBoard.getTeatimeParticipants().size() >= activatedTeatimeBoard.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEATIME_PARTICIPANT_FULL.getMessage());
        }

        if (teatimeParticipantRepository.existsByTeatimeBoardAndUser(activatedTeatimeBoard, activatedUser)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, TEATIME_PARTICIPANT_ALREADY_EXISTS.getMessage());
        }
    }

    private void validateCancelRequest(TeatimeBoard activatedTeatimeBoard, User activatedUser) {
        if (Objects.equals(activatedTeatimeBoard.getUser().getId(), activatedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_BOARD_WRITER.getMessage());
        }

        validateEndDate(activatedTeatimeBoard.getEndDate());
    }

    private void validateEndDate(LocalDateTime endDate) {
        if (LocalDateTime.now().isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEATIME_PARTICIPANT_AFTER_END_DATE.getMessage());
        }
    }
}
