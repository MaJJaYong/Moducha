package com.example.restea.teatime.repository;

import com.example.restea.teatime.entity.TeatimeBoard;
import com.example.restea.teatime.entity.TeatimeComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeatimeCommentRepository extends JpaRepository<TeatimeComment, Integer> {
    Page<TeatimeComment> findAllByTeatimeBoard(TeatimeBoard teatimeBoard, Pageable pageable);

    Long countAllByTeatimeBoard(TeatimeBoard teatimeBoard);
}
