package com.example.restea.share.entity;

import com.example.restea.common.entity.BaseTimeEntity;
import com.example.restea.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "share_comment")
public class ShareComment extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "share_comment_id")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String content;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean activated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_board_id", nullable = false)
    private ShareBoard shareBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "shareComment") // 댓글이 삭제되어도 대댓글은 보존
    private List<ShareReply> shareReplies = new ArrayList<>();

    @Builder
    public ShareComment(String content, ShareBoard shareBoard, User user) {
        this.content = content;
        this.shareBoard = shareBoard;
        this.user = user;
    }

    // 보여질 댓글 내용을 반환하는 메소드
    public String getExposedContent() {
        return activated ? content : "삭제된 댓글입니다.";
    }

    // 보여질 닉네임을 반환하는 메소드
    public String getExposedNickname() {
        return activated ? user.getExposedNickname() : "";
    }

    public void deactivate() {
        this.activated = false;
    }
}