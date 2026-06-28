package com.bountygrid.dto;

import com.bountygrid.entity.Alert.AlertCategory;
import com.bountygrid.entity.Alert.AlertType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AlertRequest(
        @NotBlank String title,
        String description,
        @NotNull AlertType alertType,
        @NotNull AlertCategory category,
        @NotNull Double latitude,
        @NotNull Double longitude,
        String city,
        @DecimalMin("1.0") @DecimalMax("25.0") Double radiusKm,
        @PositiveOrZero Double rewardAmount) {
}
