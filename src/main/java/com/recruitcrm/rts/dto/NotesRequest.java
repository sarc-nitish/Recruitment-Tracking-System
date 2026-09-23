package com.recruitcrm.rts.dto;

import jakarta.validation.constraints.Size;

public record NotesRequest(
        @Size(max = 2000, message = "Notes can be at most 2000 characters")
        String notes
) {
}
