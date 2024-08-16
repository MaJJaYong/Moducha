package com.example.restea.oauth2.repository;

import com.example.restea.oauth2.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Integer> {

}
