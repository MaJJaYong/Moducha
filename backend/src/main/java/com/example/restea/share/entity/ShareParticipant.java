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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "share_participant")
public class ShareParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "share_participant_id")
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String phone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_board_id", nullable = false)
    private ShareBoard shareBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Builder
    public ShareParticipant(String name, String phone, String address, ShareBoard shareBoard, User user) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.shareBoard = shareBoard;
        this.user = user;
    }
}