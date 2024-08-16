package com.example.restea.live.repository;

import com.example.restea.live.entity.Live;
import com.example.restea.teatime.entity.TeatimeBoard;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveRepository extends JpaRepository<Live, String> {
    Optional<Live> findByTeatimeBoard(TeatimeBoard teatimeBoard);

    boolean existsByTeatimeBoard(TeatimeBoard teatimeBoard);
}
