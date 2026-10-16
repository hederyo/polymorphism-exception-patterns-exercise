package com.parcel.exception;

/**
 * Base checked exception for parcel-related business logic errors.
 */
public class ParcelException extends Exception {
    public ParcelException(String message) {
        super(message);
    }

    public ParcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
