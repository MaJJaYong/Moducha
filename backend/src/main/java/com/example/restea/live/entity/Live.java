package com.example.restea.live.entity;

import com.example.restea.common.entity.BaseTimeEntity;
import com.example.restea.teatime.entity.TeatimeBoard;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "live")
public class Live extends BaseTimeEntity {
    @Id
    @Column(name = "live_id", updatable = false, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teatime_board_id", nullable = false, updatable = false)
    private TeatimeBoard teatimeBoard;

    @Builder
    public Live(TeatimeBoard teatimeBoard) {
        this.id = UUID.randomUUID().toString();
        this.teatimeBoard = teatimeBoard;
    }
}
