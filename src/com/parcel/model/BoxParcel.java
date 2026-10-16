package com.parcel.model;

import com.parcel.exception.InvalidParcelException;
import com.parcel.strategy.DeliveryStrategy;

/**
 * Represents a standard Box parcel.
 * Rules:
 * - Length, Width, Height in cm (integer, > 0)
 * - Maximum weight: 50 kg
 * - Volumetric weight = (Length * Width * Height) / 3500
 * - Chargeable weight = bigger of actual weight and volumetric weight, rounded UP to next whole kg
 * - Base fee = Php 40 * chargeable weight
 */
public class BoxParcel extends Parcel {
    public static final double MAX_WEIGHT = 50.0;
    public static final double VOLUMETRIC_DIVISOR = 3500.0;
    public static final double RATE_PER_KG = 40.0;

    private int length;
    private int width;
    private int height;

    public BoxParcel(String trackingNumber, String receiverName, double weight,
                     DeliveryStrategy deliveryStrategy, int length, int width, int height)
            throws InvalidParcelException {
        super(trackingNumber, receiverName, weight, deliveryStrategy);
        validateBoxFields(weight, length, width, height, MAX_WEIGHT);
        this.length = length;
        this.width = width;
        this.height = height;
    }

    protected static void validateBoxFields(double weight, int length, int width, int height, double maxWeight)
            throws InvalidParcelException {
        if (weight > maxWeight) {
            throw new InvalidParcelException(String.format(
                    "Parcel weight cannot exceed %.1f kg (entered: %.2f kg)!", maxWeight, weight));
        }
        if (length <= 0) {
            throw new InvalidParcelException("Length must be an integer greater than 0 cm!");
        }
        if (width <= 0) {
            throw new InvalidParcelException("Width must be an integer greater than 0 cm!");
        }
        if (height <= 0) {
            throw new InvalidParcelException("Height must be an integer greater than 0 cm!");
        }
    }

    /**
     * Calculates volumetric weight in kilograms.
     * Formula: (Length * Width * Height) / 3500.0
     *
     * @return volumetric weight in kg
     */
    public double getVolumetricWeight() {
        return ((double) length * width * height) / VOLUMETRIC_DIVISOR;
    }

    /**
     * Chargeable weight is the bigger of actual weight and volumetric weight,
     * rounded UP to the next whole kg (e.g. 2.0 -> 2, 2.1 -> 3).
     *
     * @return rounded up whole kg
     */
    @Override
    public double getChargeableWeight() {
        double maxWeight = Math.max(getWeight(), getVolumetricWeight());
        return Math.ceil(maxWeight);
    }

    @Override
    public double calculateBaseFee() {
        return RATE_PER_KG * getChargeableWeight();
    }

    @Override
    public String getParcelType() {
        return "Box";
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setDimensions(int length, int width, int height) throws InvalidParcelException {
        if (length <= 0 || width <= 0 || height <= 0) {
            throw new InvalidParcelException("Dimensions must be integers greater than 0 cm!");
        }
        this.length = length;
        this.width = width;
        this.height = height;
    }
}
