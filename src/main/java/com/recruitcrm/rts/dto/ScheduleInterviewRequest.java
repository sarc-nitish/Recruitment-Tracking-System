package com.recruitcrm.rts.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record ScheduleInterviewRequest(
        @NotNull(message = "Test score is required")
        @DecimalMin(value = "0.0", message = "Test score cannot be less than 0")
        @DecimalMax(value = "100.0", message = "Test score cannot be more than 100")
        Double testScore,

        @NotBlank(message = "Meet link is required")
        @Pattern(regexp = "^https?://\\S+$", message = "Meet link must start with http:// or https://")
        String meetLink,

        @NotNull(message = "Interview time is required")
        @Future(message = "Interview time must be in the future")
        LocalDateTime interviewAt
) {
}
