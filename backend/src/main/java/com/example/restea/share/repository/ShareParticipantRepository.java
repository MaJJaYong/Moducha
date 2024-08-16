package com.example.restea.share.repository;

import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareParticipant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ShareParticipantRepository extends JpaRepository<ShareParticipant, Integer> {

    Long countByShareBoard(ShareBoard shareBoard);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShareParticipant sp WHERE sp.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    List<ShareParticipant> findAllByShareBoardId(Integer shareBoardId);
}
