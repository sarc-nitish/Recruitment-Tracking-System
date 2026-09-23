package com.recruitcrm.rts.exception;

/** Logged in, but this data belongs to someone else / another company  ->  HTTP 403 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
