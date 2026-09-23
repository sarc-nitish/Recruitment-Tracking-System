package com.recruitcrm.rts.exception;

/** Duplicate data (e.g. email already registered, already applied)  ->  HTTP 409 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
