package com.parcel.model;

import com.parcel.exception.InvalidParcelException;
import com.parcel.strategy.DeliveryStrategy;

/**
 * Represents a Fragile parcel.
 * Rules:
 * - Must extend BoxParcel
 * - Additional field: Handling Note (string, non-empty)
 * - Maximum weight: 30 kg
 * - Chargeable weight = same rule as BOX
 * - Base fee = BOX base fee + Php 100 handling fee
 */
public class FragileParcel extends BoxParcel {
    public static final double MAX_FRAGILE_WEIGHT = 30.0;
    public static final double HANDLING_FEE = 100.0;

    private String handlingNote;

    public FragileParcel(String trackingNumber, String receiverName, double weight,
                         DeliveryStrategy deliveryStrategy, int length, int width, int height,
                         String handlingNote) throws InvalidParcelException {
        super(trackingNumber, receiverName, weight, deliveryStrategy, length, width, height);
        validateFragileFields(weight, handlingNote);
        this.handlingNote = handlingNote.trim();
    }

    private static void validateFragileFields(double weight, String handlingNote)
            throws InvalidParcelException {
        if (weight > MAX_FRAGILE_WEIGHT) {
            throw new InvalidParcelException(String.format(
                    "Fragile parcel weight cannot exceed %.1f kg (entered: %.2f kg)!", MAX_FRAGILE_WEIGHT, weight));
        }
        if (handlingNote == null || handlingNote.trim().isEmpty()) {
            throw new InvalidParcelException("Handling note cannot be empty for fragile parcels!");
        }
    }

    /**
     * Base fee for Fragile parcel is the standard Box base fee plus Php 100 handling fee.
     * Demonstrates polymorphic method overriding and reuse of superclass calculation.
     */
    @Override
    public double calculateBaseFee() {
        return super.calculateBaseFee() + HANDLING_FEE;
    }

    @Override
    public String getParcelType() {
        return "Fragile";
    }

    public String getHandlingNote() {
        return handlingNote;
    }

    public void setHandlingNote(String handlingNote) throws InvalidParcelException {
        if (handlingNote == null || handlingNote.trim().isEmpty()) {
            throw new InvalidParcelException("Handling note cannot be empty for fragile parcels!");
        }
        this.handlingNote = handlingNote.trim();
    }
}
