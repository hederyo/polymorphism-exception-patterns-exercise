package com.parcel.exception;

/**
 * Thrown when attempting an invalid operation on an already delivered parcel.
 */
public class ParcelAlreadyDeliveredException extends ParcelException {
    private final String trackingNumber;

    public ParcelAlreadyDeliveredException(String trackingNumber) {
        super("Parcel already delivered: " + trackingNumber);
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
