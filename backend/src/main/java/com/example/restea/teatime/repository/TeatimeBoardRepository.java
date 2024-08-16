package com.example.restea.teatime.repository;

import com.example.restea.teatime.entity.TeatimeBoard;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeatimeBoardRepository extends JpaRepository<TeatimeBoard, Integer> {

    // sort = latest
    Long countByActivated(boolean b);

    Page<TeatimeBoard> findAllByActivated(boolean b, Pageable pageable);

    // sort = urgent
    Long countByActivatedAndEndDateAfter(boolean b, Object o);

    Page<TeatimeBoard> findAllByActivatedAndEndDateAfter(boolean b, LocalDateTime now, PageRequest endDate);

    Page<TeatimeBoard> findAllByActivatedAndUserId(boolean b, Integer userId, Pageable pageable);

    Optional<TeatimeBoard> findByIdAndActivated(Integer id, boolean b);


    // keyword search
    Page<TeatimeBoard> findAllByTitleContainingAndActivatedAndEndDateAfter(String title, boolean activated,
                                                                           LocalDateTime endDate,
                                                                           PageRequest pageRequest);

    Page<TeatimeBoard> findAllByUser_NicknameContainingAndActivatedAndEndDateAfter(String writerName, boolean activated,
                                                                                   LocalDateTime endDate,
                                                                                   PageRequest pageRequest);

    Page<TeatimeBoard> findAllByContentContainingAndActivatedAndEndDateAfter(String content, boolean activated,
                                                                             LocalDateTime endDate,
                                                                             PageRequest pageRequest);

    Page<TeatimeBoard> findAllByTitleContainingAndActivated(String title, boolean activated, PageRequest pageRequest);

    Page<TeatimeBoard> findAllByUser_NicknameContainingAndActivated(String writerName, boolean activated,
                                                                    PageRequest pageRequest);

    Page<TeatimeBoard> findAllByContentContainingAndActivated(String content, boolean activated,
                                                              PageRequest pageRequest);

    @Query(value = "select t from TeatimeBoard t "
            + "left join fetch TeatimeParticipant p on t.id = p.teatimeBoard.id "
            + "where t.activated = true and t.broadcastDate > :timeOffsetMinutes "
            + "and (t.user.id = :userId or p.user.id = :userId)")
    Page<TeatimeBoard> findMyTeatimeList(@Param("userId") Integer userId,
                                         @Param("timeOffsetMinutes") LocalDateTime timeOffsetMinutes,
                                         Pageable pageable);
}