package com.parcel.strategy;

import com.parcel.exception.ServiceNotAllowedException;

/**
 * Strategy interface defining the contract for calculating delivery fees
 * and enforcing service-specific eligibility constraints.
 */
public interface DeliveryStrategy {
    /**
     * Calculates the delivery fee given a parcel's base fee.
     *
     * @param baseFee the parcel base fee in Philippine Pesos (Php)
     * @return the calculated total delivery fee
     */
    double calculateDeliveryFee(double baseFee);

    /**
     * Validates whether a parcel with the given chargeable weight is eligible
     * for this delivery service.
     *
     * @param chargeableWeight chargeable weight in kg
     * @throws ServiceNotAllowedException if the parcel exceeds the weight threshold
     */
    void validateEligibility(double chargeableWeight) throws ServiceNotAllowedException;

    /**
     * Checks whether a parcel with the given chargeable weight is eligible.
     *
     * @param chargeableWeight chargeable weight in kg
     * @return true if eligible, false otherwise
     */
    boolean isEligible(double chargeableWeight);

    /**
     * Gets the associated delivery service type enum.
     *
     * @return DeliveryServiceType
     */
    DeliveryServiceType getServiceType();

    /**
     * Gets the display name of the delivery service.
     *
     * @return string representation of service name
     */
    default String getServiceName() {
        return getServiceType().getDisplayName();
    }
}
