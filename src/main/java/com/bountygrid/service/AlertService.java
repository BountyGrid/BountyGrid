package com.bountygrid.service;

import com.bountygrid.dto.AlertRequest;
import com.bountygrid.dto.AlertResponse;
import com.bountygrid.entity.Alert;
import com.bountygrid.entity.Alert.AlertStatus;
import com.bountygrid.entity.Alert.EscrowStatus;
import com.bountygrid.entity.Tip;
import com.bountygrid.entity.Transaction;
import com.bountygrid.entity.Transaction.TransactionType;
import com.bountygrid.entity.User;
import com.bountygrid.exception.AlertNotFoundException;
import com.bountygrid.exception.InsufficientBalanceException;
import com.bountygrid.repository.AlertRepository;
import com.bountygrid.repository.TipRepository;
import com.bountygrid.repository.TransactionRepository;
import com.bountygrid.repository.UserRepository;
import com.bountygrid.util.FileUtils;
import com.bountygrid.util.GeoUtils;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final TipRepository tipRepository;
    private final TransactionRepository transactionRepository;

    @Value("${bountygrid.upload-dir:uploads}")
    private String uploadDir;

    public List<Alert> getActiveAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.ACTIVE);
    }

    public Alert getById(Long id) {
        return alertRepository.findById(id).orElseThrow(() -> new AlertNotFoundException(id));
    }

    @Transactional
    public Alert create(User owner, AlertRequest request, MultipartFile photo) {
        double reward = request.rewardAmount() == null ? 0.0 : request.rewardAmount();
        if (reward > owner.getWalletBalance()) {
            throw new InsufficientBalanceException();
        }
        Alert alert = Alert.builder()
                .owner(owner)
                .title(request.title())
                .description(request.description())
                .alertType(request.alertType())
                .category(request.category())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .city(request.city())
                .radiusKm(request.radiusKm() == null ? 5.0 : request.radiusKm())
                .rewardAmount(reward)
                .escrowStatus(reward > 0 ? EscrowStatus.LOCKED : null)
                .build();
        if (reward > 0) {
            owner.setWalletBalance(owner.getWalletBalance() - reward);
            transactionRepository.save(Transaction.builder()
                    .user(owner)
                    .type(TransactionType.ESCROW_LOCK)
                    .amount(-reward)
                    .description("Escrow locked for alert")
                    .build());
        }
        if (photo != null && !photo.isEmpty()) {
            alert.setPhotoUrl(storePhoto(photo));
        }
        owner.addPoints(10);
        userRepository.save(owner);
        return alertRepository.save(alert);
    }

    public List<AlertResponse> findNearby(double lat, double lng, double radiusKm) {
        GeoUtils.Bounds bounds = GeoUtils.boundingBox(lat, lng, radiusKm);
        return alertRepository.findInBoundingBox(AlertStatus.ACTIVE, bounds.minLat(), bounds.maxLat(), bounds.minLng(), bounds.maxLng())
                .stream()
                .map(alert -> AlertResponse.from(alert, GeoUtils.haversine(lat, lng, alert.getLatitude(), alert.getLongitude())))
                .filter(response -> response.distanceKm() <= radiusKm)
                .sorted(Comparator.comparing(AlertResponse::distanceKm))
                .toList();
    }

    @Transactional
    public Tip submitTip(User tipper, Long alertId, String content) {
        Alert alert = getById(alertId);
        tipper.addPoints(5);
        userRepository.save(tipper);
        return tipRepository.save(Tip.builder().alert(alert).tipper(tipper).content(content).build());
    }

    @Transactional
    public Alert resolveAlert(User owner, Long alertId, User finder) {
        Alert alert = getById(alertId);
        if (!alert.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the owner can resolve this alert");
        }
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new IllegalArgumentException("Alert is already resolved");
        }
        double reward = alert.getRewardAmount() == null ? 0.0 : alert.getRewardAmount();
        if (reward > 0 && alert.getEscrowStatus() == EscrowStatus.LOCKED) {
            double fee = reward * 0.05;
            double net = reward - fee;
            finder.setWalletBalance(finder.getWalletBalance() + net);
            finder.setFinds(finder.getFinds() + 1);
            finder.addPoints(100);
            transactionRepository.save(Transaction.builder()
                    .user(finder).type(TransactionType.REWARD_IN).amount(net).description("Finder reward").build());
            transactionRepository.save(Transaction.builder()
                    .type(TransactionType.FEE).amount(-fee).description("Platform fee").build());
            alert.setEscrowStatus(EscrowStatus.RELEASED);
            userRepository.save(finder);
        }
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    private String storePhoto(MultipartFile photo) {
        try {
            String filename = FileUtils.sanitize(photo.getOriginalFilename());
            Path folder = Path.of(uploadDir, "alerts");
            Files.createDirectories(folder);
            Path target = folder.resolve(filename);
            photo.transferTo(target);
            return "/uploads/alerts/" + filename;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not store uploaded file", ex);
        }
    }
}
