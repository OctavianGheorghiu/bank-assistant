package com.bankassistant.accounts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** A search filter that can never match, such as from after to, or a limit out of range. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFilterException extends RuntimeException {

    public InvalidFilterException(String message) {
        super(message);
    }
}
