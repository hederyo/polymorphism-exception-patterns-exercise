package com.parcel.exception;

/**
 * Thrown when attempting to add a parcel with a tracking number that already exists.
 */
public class DuplicateTrackingNumberException extends ParcelException {
    private final String trackingNumber;

    public DuplicateTrackingNumberException(String trackingNumber) {
        super("Tracking number already exists: " + trackingNumber);
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
