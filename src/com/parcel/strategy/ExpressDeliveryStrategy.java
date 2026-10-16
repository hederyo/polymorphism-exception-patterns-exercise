package com.parcel.strategy;

import com.parcel.exception.ServiceNotAllowedException;

/**
 * Express Delivery Strategy:
 * Delivery fee = Base fee * 1.5.
 * Applicable to parcels of all chargeable weights.
 */
public class ExpressDeliveryStrategy implements DeliveryStrategy {

    @Override
    public double calculateDeliveryFee(double baseFee) {
        return baseFee * 1.5;
    }

    @Override
    public void validateEligibility(double chargeableWeight) throws ServiceNotAllowedException {
        // Express delivery has no additional weight restrictions
    }

    @Override
    public boolean isEligible(double chargeableWeight) {
        return true;
    }

    @Override
    public DeliveryServiceType getServiceType() {
        return DeliveryServiceType.EXPRESS;
    }
}
