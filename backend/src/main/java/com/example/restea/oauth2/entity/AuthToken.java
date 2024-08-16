package com.example.restea.oauth2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auth_token")
public class AuthToken {
    @Id
    @GeneratedValue
    @Column(name = "auth_token_id")
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Builder
    public AuthToken(String value) {
        this.value = value;
    }
}
