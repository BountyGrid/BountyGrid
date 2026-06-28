package com.bountygrid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoryRequest(
        @NotNull Long alertId,
        @NotBlank String title,
        @NotBlank String story,
        Integer recoveryTimeHours,
        Boolean publicStory) {
}
