package com.bountygrid.repository;

import com.bountygrid.entity.Tip;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipRepository extends JpaRepository<Tip, Long> {
    List<Tip> findByAlertIdOrderByCreatedAtDesc(Long alertId);
}
