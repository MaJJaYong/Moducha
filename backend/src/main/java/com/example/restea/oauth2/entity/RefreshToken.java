package com.example.restea.oauth2.entity;

import com.example.restea.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue
    @Column(name = "refresh_token_id")
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean revoked;

    @OneToOne(mappedBy = "refreshToken", fetch = FetchType.LAZY)
    private User user;

    @Builder
    public RefreshToken(String value, LocalDateTime issuedAt, LocalDateTime expiredAt) {
        this.value = value;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
    }
}