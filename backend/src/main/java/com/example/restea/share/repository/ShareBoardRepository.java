package com.example.restea.share.repository;

import com.example.restea.share.entity.ShareBoard;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareBoardRepository extends JpaRepository<ShareBoard, Integer> {

    // sort = latest
    Long countByActivated(boolean b);

    // sort = urgent
    Long countByActivatedAndEndDateAfter(boolean b, Object o);

    Page<ShareBoard> findAllByActivatedAndUserId(boolean b, Integer userId, Pageable pageable);

    Optional<ShareBoard> findByIdAndActivated(Integer id, boolean b);

    Long countByActivatedAndUserId(boolean b, Integer userId);

}