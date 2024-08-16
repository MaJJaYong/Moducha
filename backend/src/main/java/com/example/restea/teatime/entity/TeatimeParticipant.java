package com.example.restea.teatime.entity;

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
@Table(name = "teatime_participant")
public class TeatimeParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "teatime_participant_id")
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String phone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teatime_board_id", nullable = false)
    private TeatimeBoard teatimeBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Builder
    public TeatimeParticipant(String name, String phone, String address, TeatimeBoard teatimeBoard, User user) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.teatimeBoard = teatimeBoard;
        this.user = user;
    }
}