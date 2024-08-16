package com.example.restea.user.repository;

import com.example.restea.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByAuthIdAndActivated(String username, boolean b);

//    Optional<User> findByAuthIdAndActivated(String username, boolean b);

    boolean existsByNickname(String nickname);
}
