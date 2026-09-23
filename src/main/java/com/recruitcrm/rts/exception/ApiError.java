package com.recruitcrm.rts.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** The one JSON shape used for EVERY error the API returns. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(LocalDateTime timestamp,
                       int status,
                       String error,
                       String message,
                       Map<String, String> fieldErrors) {
}
