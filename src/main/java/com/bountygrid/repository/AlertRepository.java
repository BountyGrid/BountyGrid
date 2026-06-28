package com.bountygrid.repository;

import com.bountygrid.entity.Alert;
import com.bountygrid.entity.Alert.AlertStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);
    List<Alert> findBySosTrueAndStatus(AlertStatus status);

    @Query("""
            select a from Alert a
            where a.status = com.bountygrid.entity.Alert$AlertStatus.ACTIVE
              and a.status = :status
              and a.latitude between :minLat and :maxLat
              and a.longitude between :minLng and :maxLng
            """)
    List<Alert> findInBoundingBox(
            @Param("status") AlertStatus status,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng);
}
