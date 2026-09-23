package com.recruitcrm.rts.exception;

/** The request is understood but breaks a business rule (e.g. wrong pipeline stage)  ->  HTTP 400 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
