package com.parcel.strategy;

import com.parcel.exception.ServiceNotAllowedException;

/**
 * Standard Delivery Strategy:
 * Delivery fee = Base fee.
 * Applicable to parcels of all chargeable weights.
 */
public class StandardDeliveryStrategy implements DeliveryStrategy {

    @Override
    public double calculateDeliveryFee(double baseFee) {
        return baseFee;
    }

    @Override
    public void validateEligibility(double chargeableWeight) throws ServiceNotAllowedException {
        // Standard delivery has no additional weight restrictions
    }

    @Override
    public boolean isEligible(double chargeableWeight) {
        return true;
    }

    @Override
    public DeliveryServiceType getServiceType() {
        return DeliveryServiceType.STANDARD;
    }
}
