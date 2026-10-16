package com.parcel.strategy;

/**
 * Enumeration representing available delivery service tiers.
 */
public enum DeliveryServiceType {
    STANDARD("Standard"),
    EXPRESS("Express"),
    SAME_DAY("Same Day");

    private final String displayName;

    DeliveryServiceType(String displayName) {
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
