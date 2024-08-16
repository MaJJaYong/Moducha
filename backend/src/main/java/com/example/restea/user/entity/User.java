package com.example.restea.user.entity;


import com.example.restea.common.entity.BaseTimeEntity;
import com.example.restea.oauth2.entity.AuthToken;
import com.example.restea.oauth2.entity.RefreshToken;
import com.example.restea.record.entity.Record;
import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareComment;
import com.example.restea.share.entity.ShareParticipant;
import com.example.restea.share.entity.ShareReply;
import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import com.example.restea.teatime.entity.TeatimeParticipant;
import com.example.restea.teatime.entity.TeatimeReply;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Transient
    private static final String defaultPicture = "https://avatars.githubusercontent.com/u/121084350?v=4";

    @Transient
    private static final String deactivatedPicture = "https://cdn-icons-png.flaticon.com/128/9502/9502048.png";

    @Id
    @GeneratedValue
    @Column(name = "users_id")
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false)
    private String picture;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'USER'")
    private ROLE role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String authId;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean activated;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "auth_token_id")
    private AuthToken authToken;

    // 기록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Record> records = new ArrayList<>();


    // 나눔 게시판 관련
    @OneToMany(mappedBy = "user") // 글은 유저 삭제 후에도 보존
    private List<ShareBoard> shareBoards = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ShareComment> shareComments = new ArrayList<>(); // 댓글은 유저 삭제 후에도 보존

    @OneToMany(mappedBy = "user")
    private List<ShareReply> shareReplies = new ArrayList<>(); // 대댓글은 유저 삭제 후에도 보존

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShareParticipant> shareParticipants = new ArrayList<>();


    // 티타임 게시판 관련
    @OneToMany(mappedBy = "user") // 글은 유저 삭제 후에도 보존
    private List<TeatimeBoard> teatimeBoards = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<TeatimeComment> teatimeComments = new ArrayList<>(); // 댓글은 유저 삭제 후에도 보존

    @OneToMany(mappedBy = "user")
    private List<TeatimeReply> teatimeReplies = new ArrayList<>(); // 대댓글은 유저 삭제 후에도 보존

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeatimeParticipant> teatimeParticipants = new ArrayList<>();

    @Builder
    public User(String nickname, String authId, AuthToken authToken, String picture) {
        this.nickname = nickname;
        this.authId = authId;
        this.authToken = authToken;
        this.picture = picture == null ? defaultPicture : picture;
    }

    public void addRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void deleteRefreshToken() {
        this.refreshToken = null;
    }

    // 보여질 닉네임을 반환하는 메소드
    public String getExposedNickname() {
        return activated ? nickname : "탈퇴한 유저";
    }

    public void deactivate() {
        this.activated = false;
    }

    public void clearRecords() {
        records.clear(); // 기록 삭제
    }

    public void clearParticipants() {
        shareParticipants.clear(); // 나눔 게시판 clear
        teatimeParticipants.clear(); // 티타임 게시판 clear
    }

    public void deleteAuthToken() {
        this.authToken = null;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePicture(String picture) {
        this.picture = picture;
    }

    public void changeDeactivatedPicture() {
        this.picture = deactivatedPicture;
    }
}
