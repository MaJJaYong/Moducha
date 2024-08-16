package com.example.restea.live.service;

import static com.example.restea.live.enums.LiveMessage.LIVEKIT_BAD_REQUEST;
import static com.example.restea.live.enums.LiveMessage.LIVE_NOT_FOUND;
import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_AFTER_BROADCAST_DATE;
import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_BEFORE_BROADCAST_DATE;
import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_NOT_ACTIVATED;
import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_NOT_BROADCAST_DATE;
import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_NOT_FOUND;
import static com.example.restea.teatime.enums.TeatimeBoardMessage.TEATIME_BOARD_NOT_WRITER;
import static com.example.restea.teatime.enums.TeatimeParticipantMessage.TEATIME_PARTICIPANT_NOT_FOUND;
import static com.example.restea.user.enums.UserMessage.USER_NOT_ACTIVATED;
import static com.example.restea.user.enums.UserMessage.USER_NOT_FOUND;

import com.example.restea.live.dto.LiveMuteRequestDTO;
import com.example.restea.live.entity.Live;
import com.example.restea.live.repository.LiveRepository;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.repository.TeatimeBoardRepository;
import com.example.restea.teatime.repository.TeatimeParticipantRepository;
import com.example.restea.user.entity.User;
import com.example.restea.user.repository.UserRepository;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.RoomServiceClient;
import io.livekit.server.WebhookReceiver;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import livekit.LivekitModels;
import livekit.LivekitWebhook.WebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import retrofit2.Call;
import retrofit2.Response;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveService {

    private final UserRepository userRepository;
    private final LiveRepository liveRepository;
    private final TeatimeBoardRepository teatimeBoardRepository;
    private final TeatimeParticipantRepository teatimeParticipantRepository;

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    @Value("${host.url}")
    private String HOST_URL;

    private RoomServiceClient client;

    @PostConstruct
    public void init() {
        this.client = RoomServiceClient.create(HOST_URL, LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
    }

    public boolean isLiveOpen(Integer teatimeBoardId, Integer userId) {

        User activatedUser = getActivatedUser(userId);
        TeatimeBoard teatimeBoard = getActivatedTeatimeBoardAndWriter(teatimeBoardId);

        checkParticipant(teatimeBoard, activatedUser);

        return liveRepository.existsByTeatimeBoard(teatimeBoard);
    }

    @Transactional
    public AccessToken createLive(Integer teatimeBoardId, Integer userId) {

        User activatedUser = getActivatedUser(userId);
        TeatimeBoard teatimeBoard = getActivatedTeatimeBoardAndWriter(teatimeBoardId);

        checkWriter(teatimeBoard, activatedUser);
        checkBroadCastDate(teatimeBoard);

        Live live = Live.builder()
                .teatimeBoard(teatimeBoard)
                .build();

        liveRepository.save(live);

        return createToken(live.getId(), activatedUser);
    }

    public AccessToken liveJoin(Integer teatimeBoardId, Integer userId) {

        User activatedUser = getActivatedUser(userId);
        TeatimeBoard teatimeBoard = getActivatedTeatimeBoardAndWriter(teatimeBoardId);

        checkParticipant(teatimeBoard, activatedUser);

        Live live = liveRepository.findByTeatimeBoard(teatimeBoard)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, LIVE_NOT_FOUND.getMessage()));

        return createToken(live.getId(), activatedUser);
    }

    public void webHook(String authHeader, String body) {
        WebhookReceiver webhookReceiver = new WebhookReceiver(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        try {
            WebhookEvent event = webhookReceiver.receive(body, authHeader);

            if (event.getEvent().equals("participant_left")) {
                String liveId = event.getRoom().getName();
                Integer userId = Integer.parseInt(event.getParticipant().getIdentity());

                Optional<Live> liveOptional = liveRepository.findById(liveId);
                if (liveOptional.isEmpty()) {
                    return;
                }
                Live live = liveOptional.get();

                TeatimeBoard teatimeBoard = live.getTeatimeBoard();

                if (teatimeBoard.getUser().getId() != userId) {
                    return;
                }

                Call<Void> deleteCall = client.deleteRoom(liveId);
                Response<Void> deleteResponse = deleteCall.execute();

                if (deleteResponse.isSuccessful()) {
                    liveRepository.deleteById(liveId);
                }
            }

        } catch (Exception e) {
            log.error("Error validating webhook event: " + e.getMessage());
        }
    }

    public void liveKick(Integer teatimeBoardId, Integer kickUserId, Integer userId) {

        User activatedUser = getActivatedUser(userId);
        TeatimeBoard teatimeBoard = getActivatedTeatimeBoardAndWriter(teatimeBoardId);

        checkWriter(teatimeBoard, activatedUser);

        Live live = liveRepository.findByTeatimeBoard(teatimeBoard)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, LIVE_NOT_FOUND.getMessage()));

        try {
            Call<Void> deleteCall = client.removeParticipant(live.getId(), kickUserId.toString());
            Response<Void> deleteResponse = deleteCall.execute();
            if (deleteResponse.isSuccessful()) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, deleteResponse.errorBody().string());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, LIVEKIT_BAD_REQUEST.getMessage());
        }
    }

    public void liveMute(Integer teatimeBoardId, LiveMuteRequestDTO request, Integer userId) {

        User activatedUser = getActivatedUser(userId);
        TeatimeBoard teatimeBoard = getActivatedTeatimeBoardAndWriter(teatimeBoardId);

        checkWriter(teatimeBoard, activatedUser);

        Live live = liveRepository.findByTeatimeBoard(teatimeBoard)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, LIVE_NOT_FOUND.getMessage()));

        try {
            Call<LivekitModels.TrackInfo> muteCall = client.mutePublishedTrack(live.getId(),
                    request.getUserId().toString(),
                    request.getTrackSid(),
                    request.getIsMute());
            Response<LivekitModels.TrackInfo> muteResponse = muteCall.execute();
            if (muteResponse.isSuccessful()) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, muteResponse.errorBody().string());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, LIVEKIT_BAD_REQUEST.getMessage());
        }
    }

    // 티타임 게시글 작성자인지 확인하는 메소드
    private void checkWriter(TeatimeBoard teatimeBoard, User user) {
        if (!teatimeBoard.getUser().equals(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_BOARD_NOT_WRITER.getMessage());
        }
    }

    // 티타임 방송 참가자인지 확인하는 메소드
    private void checkParticipant(TeatimeBoard teatimeBoard, User user) {
        if (teatimeBoard.getUser().equals(user)) {
            return;
        }

        if (!teatimeParticipantRepository.existsByTeatimeBoardAndUser(teatimeBoard, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_PARTICIPANT_NOT_FOUND.getMessage());
        }
    }

    // 방송 예정일인지 확인하는 메소드
    private void checkBroadCastDate(TeatimeBoard teatimeBoard) {
        LocalDateTime broadcastDate = teatimeBoard.getBroadcastDate();
        LocalDateTime now = LocalDateTime.now();

        // 방송 예정일이랑 다른 날인 경우
        if (!broadcastDate.toLocalDate().isEqual(now.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_BOARD_NOT_BROADCAST_DATE.getMessage());
        }

        // 방송 예정일보다 현재 시간보다 30분전인 경우
        if (now.isBefore(broadcastDate.minusMinutes(30))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_BOARD_BEFORE_BROADCAST_DATE.getMessage());
        }

        // 방송 예정일보다 현재 시간보다 30분후인 경우
        if (now.isAfter(broadcastDate.plusMinutes(30))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEATIME_BOARD_AFTER_BROADCAST_DATE.getMessage());
        }
    }

    // 티타임 게시글 존재하는지 확인하는 메소드
    private TeatimeBoard getActivatedTeatimeBoardAndWriter(Integer teatimeBoardId) {
        TeatimeBoard teatimeBoard = teatimeBoardRepository.findByIdAndActivated(teatimeBoardId, true)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, TEATIME_BOARD_NOT_FOUND.getMessage()));

        // 티타임 게시글 삭제 여부 확인
        if (!teatimeBoard.getActivated()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEATIME_BOARD_NOT_ACTIVATED.getMessage());
        }

        return teatimeBoard;
    }

    // AccessToken 발급하는 메소드
    private AccessToken createToken(String liveId, User user) {
        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        token.setName(user.getNickname());
        token.setIdentity(user.getId().toString());
        token.addGrants(new RoomJoin(true), new RoomName(liveId));

        return token;
    }

    public User getActivatedUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND.getMessage()));
        if (!user.getActivated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_ACTIVATED.getMessage());
        }
        return user;
    }
}
