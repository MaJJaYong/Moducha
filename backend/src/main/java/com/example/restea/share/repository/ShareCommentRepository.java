package com.example.restea.share.repository;


import com.example.restea.share.entity.ShareBoard;
import com.example.restea.share.entity.ShareComment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareCommentRepository extends JpaRepository<ShareComment, Integer> {
    Page<ShareComment> findAllByShareBoard(ShareBoard shareBoard, Pageable pageable);

    Long countAllByShareBoard(ShareBoard shareBoard);

    Optional<ShareComment> findByIdAndActivated(Integer shareCommentId, boolean b);
}
