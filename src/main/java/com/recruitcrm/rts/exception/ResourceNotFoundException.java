package com.recruitcrm.rts.exception;

/** Something asked for by id does not exist  ->  HTTP 404 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
