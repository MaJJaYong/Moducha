package com.example.restea.teatime.entity;

import com.example.restea.common.entity.BaseTimeEntity;
import com.example.restea.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "teatime_board")
public class TeatimeBoard extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(name = "teatime_board_id")
    private Integer id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime broadcastDate;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer viewCount;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean activated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "teatimeBoard") // 글 비활성화 시  댓글 비활성화
    private List<TeatimeComment> teatimeComments = new ArrayList<>();

    @OneToMany(mappedBy = "teatimeBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeatimeParticipant> teatimeParticipants = new ArrayList<>();

    @Builder
    public TeatimeBoard(String title, String content, LocalDateTime broadcastDate, Integer maxParticipants,
                        LocalDateTime endDate, User user) {
        this.title = title;
        this.content = content;
        this.broadcastDate = broadcastDate;
        this.maxParticipants = maxParticipants;
        this.endDate = endDate;
        this.user = user;
    }

    public void addViewCount() {
        this.viewCount++;
    }

    public void deactivate() {
        this.activated = false;
    }

    public void update(String title, String content, Integer maxParticipants, LocalDateTime endDate,
                       LocalDateTime broadcastDate) {
        this.title = title;
        this.content = content;
        this.maxParticipants = maxParticipants;
        this.endDate = endDate;
        this.broadcastDate = broadcastDate;
    }
}