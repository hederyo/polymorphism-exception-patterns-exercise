package com.parcel.exception;

/**
 * Thrown when parcel attributes violate domain validation rules.
 */
public class InvalidParcelException extends ParcelException {
    public InvalidParcelException(String message) {
        super(message);
    }
}
