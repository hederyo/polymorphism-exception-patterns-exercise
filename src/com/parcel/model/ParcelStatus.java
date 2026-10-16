package com.parcel.model;

/**
 * Enumeration representing the delivery status of a parcel.
 */
public enum ParcelStatus {
    PENDING("Pending"),
    DELIVERED("Delivered");

    private final String displayName;

    ParcelStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
