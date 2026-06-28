package com.bountygrid.repository;

import com.bountygrid.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("select u from User u order by u.points desc, u.finds desc")
    java.util.List<User> findTopByPoints(Pageable pageable);

    @Query("select count(u) from User u where u.points > :points")
    long countUsersAbove(@Param("points") int points);

    @Modifying
    @Query("update User u set u.sosUsedThisMonth = 0, u.leaderOfMonth = false")
    void resetMonthlyStats();
}
