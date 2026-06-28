package com.bountygrid.dto;

import com.bountygrid.entity.Alert;

public record AlertResponse(
        Long id,
        String title,
        String description,
        String alertType,
        String category,
        String city,
        Double rewardAmount,
        String photoUrl,
        Double distanceKm) {
    public static AlertResponse from(Alert alert, Double distanceKm) {
        return new AlertResponse(
                alert.getId(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getAlertType().name(),
                alert.getCategory().name(),
                alert.getCity(),
                alert.getRewardAmount(),
                alert.getPhotoUrl(),
                distanceKm);
    }
}
