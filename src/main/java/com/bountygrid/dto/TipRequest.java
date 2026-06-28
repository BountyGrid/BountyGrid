package com.bountygrid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipRequest(@NotBlank @Size(max = 1200) String content) {
}
