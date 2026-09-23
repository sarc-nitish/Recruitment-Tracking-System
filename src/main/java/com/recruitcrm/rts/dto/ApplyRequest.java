package com.recruitcrm.rts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyRequest(
        @NotBlank(message = "Skills are required (comma separated, e.g. Java, MySQL)")
        @Size(max = 1000, message = "Skills can be at most 1000 characters")
        String skills,

        @Size(max = 1000, message = "Cover note can be at most 1000 characters")
        String coverNote
) {
}
