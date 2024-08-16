package com.example.restea.oauth2.repository;

import com.example.restea.oauth2.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Boolean existsByValue(String value);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.value = :value")
    void revokeByValue(@Param("value") String value);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.id = :id")
    void revokeById(@Param("id") Integer id);

    RefreshToken findByValue(String value);
}
