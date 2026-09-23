package com.recruitcrm.rts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequest(
        @NotBlank(message = "Rejection reason is required")
        @Size(max = 1000, message = "Reason can be at most 1000 characters")
        String reason
) {
}
