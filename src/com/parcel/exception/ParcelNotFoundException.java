package com.parcel.exception;

/**
 * Thrown when a parcel with the requested tracking number cannot be found.
 */
public class ParcelNotFoundException extends ParcelException {
    private final String trackingNumber;

    public ParcelNotFoundException(String trackingNumber) {
        super("Parcel not found: " + trackingNumber);
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
