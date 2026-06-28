package com.bountygrid.service;

import com.bountygrid.entity.Alert;
import com.bountygrid.entity.SosBroadcast;
import com.bountygrid.entity.User;
import com.bountygrid.repository.AlertRepository;
import com.bountygrid.repository.SosBroadcastRepository;
import com.bountygrid.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SosService {
    private final AlertService alertService;
    private final AlertRepository alertRepository;
    private final SosBroadcastRepository sosBroadcastRepository;
    private final UserRepository userRepository;

    public List<SosBroadcast> getActive() {
        return sosBroadcastRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public SosBroadcast broadcast(User user, Long alertId, double radiusKm) {
        if (user.getSosUsedThisMonth() >= 1) {
            throw new IllegalArgumentException("SOS broadcast is limited to one per month");
        }
        Alert alert = alertService.getById(alertId);
        if (!alert.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only the alert owner can broadcast SOS");
        }
        alert.setSos(true);
        user.setSosUsedThisMonth(user.getSosUsedThisMonth() + 1);
        alertRepository.save(alert);
        userRepository.save(user);
        return sosBroadcastRepository.save(SosBroadcast.builder()
                .alert(alert)
                .user(user)
                .radiusKm(radiusKm)
                .build());
    }
}
