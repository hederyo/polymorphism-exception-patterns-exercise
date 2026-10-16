package com.parcel.strategy;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory and registry providing access to DeliveryStrategy instances.
 */
public final class DeliveryStrategyFactory {
    private static final Map<DeliveryServiceType, DeliveryStrategy> STRATEGIES = new EnumMap<>(DeliveryServiceType.class);

    static {
        STRATEGIES.put(DeliveryServiceType.STANDARD, new StandardDeliveryStrategy());
        STRATEGIES.put(DeliveryServiceType.EXPRESS, new ExpressDeliveryStrategy());
        STRATEGIES.put(DeliveryServiceType.SAME_DAY, new SameDayDeliveryStrategy());
    }

    private DeliveryStrategyFactory() {
        // Prevent instantiation
    }

    /**
     * Retrieves the DeliveryStrategy for the given DeliveryServiceType.
     *
     * @param type DeliveryServiceType enum
     * @return corresponding DeliveryStrategy instance
     */
    public static DeliveryStrategy getStrategy(DeliveryServiceType type) {
        if (type == null) {
            throw new IllegalArgumentException("Delivery service type cannot be null");
        }
        DeliveryStrategy strategy = STRATEGIES.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy configured for type: " + type);
        }
        return strategy;
    }
}
