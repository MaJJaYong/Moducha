package com.example.restea.record.repository;

import com.example.restea.record.entity.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RecordRepository extends JpaRepository<Record, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Record r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    Page<Record> findAllByUserId(Integer userId, Pageable pageable);

    Long countByUserId(Integer userId);
}
