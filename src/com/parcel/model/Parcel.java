package com.parcel.model;

import com.parcel.exception.InvalidParcelException;
import com.parcel.exception.ParcelAlreadyDeliveredException;
import com.parcel.exception.ServiceNotAllowedException;
import com.parcel.strategy.DeliveryStrategy;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Abstract base class representing a parcel in the delivery system.
 * Demonstrates polymorphism through abstract domain methods and encapsulates
 * the Strategy pattern for delivery fee calculations.
 */
public abstract class Parcel {
    public static final Pattern TRACKING_NUMBER_PATTERN = Pattern.compile("^ABC\\d{4}$");

    private final String trackingNumber;
    private String receiverName;
    private double weight;
    private DeliveryStrategy deliveryStrategy;
    private ParcelStatus status;

    /**
     * Constructs a new Parcel instance.
     *
     * @param trackingNumber unique tracking number matching ABC\d{4}
     * @param receiverName   non-empty recipient name
     * @param weight         positive weight in kilograms
     * @param deliveryStrategy delivery fee strategy
     * @throws InvalidParcelException if any parameter is invalid
     */
    public Parcel(String trackingNumber, String receiverName, double weight, DeliveryStrategy deliveryStrategy)
            throws InvalidParcelException {
        validateCommonFields(trackingNumber, receiverName, weight, deliveryStrategy);
        this.trackingNumber = trackingNumber.trim().toUpperCase();
        this.receiverName = receiverName.trim();
        this.weight = weight;
        this.deliveryStrategy = deliveryStrategy;
        this.status = ParcelStatus.PENDING;
    }

    /**
     * Validates common parcel attributes.
     */
    private static void validateCommonFields(String trackingNumber, String receiverName, double weight,
                                            DeliveryStrategy strategy) throws InvalidParcelException {
        if (trackingNumber == null || !TRACKING_NUMBER_PATTERN.matcher(trackingNumber.trim().toUpperCase()).matches()) {
            throw new InvalidParcelException("Tracking number must start with 'ABC' followed by 4 digits (e.g., ABC0001)!");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new InvalidParcelException("Receiver name cannot be empty!");
        }
        if (Double.isNaN(weight) || Double.isInfinite(weight) || weight <= 0) {
            throw new InvalidParcelException("Weight must be greater than 0 kg!");
        }
        if (strategy == null) {
            throw new InvalidParcelException("Delivery service strategy cannot be null!");
        }
    }

    /**
     * Validates that tracking number string adheres to pattern.
     */
    public static boolean isValidTrackingFormat(String trackingNumber) {
        return trackingNumber != null && TRACKING_NUMBER_PATTERN.matcher(trackingNumber.trim().toUpperCase()).matches();
    }

    /**
     * Calculates the chargeable weight in kilograms according to parcel type rules.
     * Polymorphic method implemented by concrete parcel types.
     *
     * @return chargeable weight in kg
     */
    public abstract double getChargeableWeight();

    /**
     * Calculates the base delivery fee according to parcel type rules.
     * Polymorphic method implemented by concrete parcel types.
     *
     * @return base fee in Php
     */
    public abstract double calculateBaseFee();

    /**
     * Returns the human-readable parcel type name.
     *
     * @return parcel type string (e.g. Document, Box, Fragile)
     */
    public abstract String getParcelType();

    /**
     * Calculates total delivery fee using the assigned DeliveryStrategy.
     *
     * @return delivery fee in Php
     */
    public double getDeliveryFee() {
        return deliveryStrategy.calculateDeliveryFee(calculateBaseFee());
    }

    /**
     * Changes the delivery strategy of the parcel.
     * Ensures eligibility validation passes before applying change.
     *
     * @param newStrategy the new delivery strategy to apply
     * @throws ServiceNotAllowedException if the parcel exceeds strategy weight limits
     * @throws InvalidParcelException if newStrategy is null
     */
    public void setDeliveryStrategy(DeliveryStrategy newStrategy)
            throws ServiceNotAllowedException, InvalidParcelException {
        if (newStrategy == null) {
            throw new InvalidParcelException("Delivery strategy cannot be null!");
        }
        newStrategy.validateEligibility(getChargeableWeight());
        this.deliveryStrategy = newStrategy;
    }

    /**
     * Marks the parcel as delivered.
     *
     * @throws ParcelAlreadyDeliveredException if parcel is already delivered
     */
    public void markDelivered() throws ParcelAlreadyDeliveredException {
        if (this.status == ParcelStatus.DELIVERED) {
            throw new ParcelAlreadyDeliveredException(this.trackingNumber);
        }
        this.status = ParcelStatus.DELIVERED;
    }

    public boolean isDelivered() {
        return this.status == ParcelStatus.DELIVERED;
    }

    // Getters and Setters
    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) throws InvalidParcelException {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new InvalidParcelException("Receiver name cannot be empty!");
        }
        this.receiverName = receiverName.trim();
    }

    public double getWeight() {
        return weight;
    }

    protected void setWeight(double weight) throws InvalidParcelException {
        if (Double.isNaN(weight) || Double.isInfinite(weight) || weight <= 0) {
            throw new InvalidParcelException("Weight must be greater than 0 kg!");
        }
        this.weight = weight;
    }

    public DeliveryStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parcel parcel = (Parcel) o;
        return Objects.equals(trackingNumber, parcel.trackingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingNumber);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] -> %s (Weight: %.2f kg, Chargeable: %.2f kg, Service: %s, Fee: Php %.2f, Status: %s)",
                getParcelType(), trackingNumber, receiverName, weight, getChargeableWeight(),
                deliveryStrategy.getServiceName(), getDeliveryFee(), status);
    }
}
