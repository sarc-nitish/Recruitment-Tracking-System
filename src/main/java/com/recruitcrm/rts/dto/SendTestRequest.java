package com.recruitcrm.rts.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record SendTestRequest(
        @NotBlank(message = "Test link is required")
        @Pattern(regexp = "^https?://\\S+$", message = "Test link must start with http:// or https://")
        String testLink,

        @NotNull(message = "Deadline is required")
        @Future(message = "Deadline must be in the future")
        LocalDateTime deadline
) {
}
