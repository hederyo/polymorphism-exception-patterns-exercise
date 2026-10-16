package com.parcel.strategy;

import com.parcel.exception.ServiceNotAllowedException;

/**
 * Same Day Delivery Strategy:
 * Delivery fee = (Base fee * 2) + Php 100.
 * Restricted to parcels with a chargeable weight of 10 kg or less.
 */
public class SameDayDeliveryStrategy implements DeliveryStrategy {
    public static final double MAX_CHARGEABLE_WEIGHT = 10.0;
    public static final String RESTRICTION_MESSAGE = "Same Day delivery is only for parcels up to 10 kg!";

    @Override
    public double calculateDeliveryFee(double baseFee) {
        return (baseFee * 2.0) + 100.0;
    }

    @Override
    public void validateEligibility(double chargeableWeight) throws ServiceNotAllowedException {
        if (!isEligible(chargeableWeight)) {
            throw new ServiceNotAllowedException(RESTRICTION_MESSAGE);
        }
    }

    @Override
    public boolean isEligible(double chargeableWeight) {
        return chargeableWeight <= MAX_CHARGEABLE_WEIGHT;
    }

    @Override
    public DeliveryServiceType getServiceType() {
        return DeliveryServiceType.SAME_DAY;
    }
}
