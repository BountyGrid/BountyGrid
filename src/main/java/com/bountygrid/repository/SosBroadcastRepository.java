package com.bountygrid.repository;

import com.bountygrid.entity.SosBroadcast;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SosBroadcastRepository extends JpaRepository<SosBroadcast, Long> {
    List<SosBroadcast> findByActiveTrueOrderByCreatedAtDesc();
}
