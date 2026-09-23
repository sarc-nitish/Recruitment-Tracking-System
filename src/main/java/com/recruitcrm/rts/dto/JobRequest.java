package com.recruitcrm.rts.dto;

import com.recruitcrm.rts.entity.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobRequest(
        @NotBlank(message = "Job title is required")
        String title,

        String location,

        @NotBlank(message = "Required skills are needed (comma separated, e.g. Java, Spring Boot)")
        @Size(max = 1000, message = "Required skills can be at most 1000 characters")
        String requiredSkills,

        @Size(max = 2000, message = "Description can be at most 2000 characters")
        String description,

        // optional: defaults to OPEN
        JobStatus status
) {
}
