package com.parcel.exception;

/**
 * Thrown when a delivery service is not allowed for the given parcel attributes.
 */
public class ServiceNotAllowedException extends ParcelException {
    public ServiceNotAllowedException(String message) {
        super(message);
    }
}
