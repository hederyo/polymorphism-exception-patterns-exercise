package com.parcel.service;

import com.parcel.exception.DuplicateTrackingNumberException;
import com.parcel.exception.InvalidParcelException;
import com.parcel.exception.ParcelAlreadyDeliveredException;
import com.parcel.exception.ParcelNotFoundException;
import com.parcel.exception.ServiceNotAllowedException;
import com.parcel.model.Parcel;
import com.parcel.strategy.DeliveryStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton service responsible for managing parcel records,
 * enforcing business invariants, and coordinating parcel operations.
 * Demonstrates thread-safe Singleton design pattern using the Bill Pugh Holder idiom.
 */
public class ParcelManager {

    /**
     * Lazy-loaded static inner holder ensuring thread-safe Singleton instantiation
     * without synchronization overhead or nested conditional checks.
     */
    private static class InstanceHolder {
        private static final ParcelManager INSTANCE = new ParcelManager();
    }

    private final Map<String, Parcel> parcels;

    /**
     * Private constructor to enforce Singleton design pattern.
     */
    private ParcelManager() {
        this.parcels = new LinkedHashMap<>();
    }

    /**
     * Retrieves the global Singleton instance of ParcelManager.
     *
     * @return the singleton ParcelManager instance
     */
    public static ParcelManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Adds a new parcel to the system.
     *
     * @param parcel the parcel to add
     * @throws DuplicateTrackingNumberException if tracking number already exists
     * @throws InvalidParcelException if parcel is null
     */
    public synchronized void addParcel(Parcel parcel)
            throws DuplicateTrackingNumberException, InvalidParcelException {
        if (parcel == null) {
            throw new InvalidParcelException("Parcel cannot be null!");
        }
        String key = parcel.getTrackingNumber();
        if (parcels.containsKey(key)) {
            throw new DuplicateTrackingNumberException(key);
        }
        parcels.put(key, parcel);
    }

    /**
     * Checks if a tracking number is already present.
     *
     * @param trackingNumber tracking number to check
     * @return true if exists, false otherwise
     */
    public synchronized boolean trackingNumberExists(String trackingNumber) {
        if (trackingNumber == null) {
            return false;
        }
        return parcels.containsKey(trackingNumber.trim().toUpperCase());
    }

    /**
     * Finds a parcel by tracking number.
     *
     * @param trackingNumber the tracking number to search
     * @return found Parcel
     * @throws ParcelNotFoundException if parcel does not exist
     */
    public synchronized Parcel findParcel(String trackingNumber) throws ParcelNotFoundException {
        if (trackingNumber == null) {
            throw new ParcelNotFoundException("null");
        }
        String key = trackingNumber.trim().toUpperCase();
        Parcel parcel = parcels.get(key);
        if (parcel == null) {
            throw new ParcelNotFoundException(key);
        }
        return parcel;
    }

    /**
     * Returns an unmodifiable list of all registered parcels in insertion order.
     *
     * @return list of parcels
     */
    public synchronized List<Parcel> getAllParcels() {
        return Collections.unmodifiableList(new ArrayList<>(parcels.values()));
    }

    /**
     * Returns total number of parcels registered in the system.
     */
    public synchronized int getParcelCount() {
        return parcels.size();
    }

    /**
     * Changes the delivery service for an existing pending parcel.
     *
     * @param trackingNumber tracking number of the parcel
     * @param newStrategy the new delivery strategy
     * @return the updated total delivery fee
     * @throws ParcelNotFoundException if parcel is not found
     * @throws ParcelAlreadyDeliveredException if parcel is already delivered
     * @throws ServiceNotAllowedException if service is disallowed (e.g. Same Day > 10 kg)
     * @throws InvalidParcelException if strategy is invalid
     */
    public synchronized double changeDeliveryService(String trackingNumber, DeliveryStrategy newStrategy)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException, ServiceNotAllowedException, InvalidParcelException {
        Parcel parcel = findParcel(trackingNumber);
        if (parcel.isDelivered()) {
            throw new ParcelAlreadyDeliveredException(trackingNumber);
        }
        parcel.setDeliveryStrategy(newStrategy);
        return parcel.getDeliveryFee();
    }

    /**
     * Marks an existing parcel as delivered.
     *
     * @param trackingNumber tracking number of the parcel
     * @throws ParcelNotFoundException if parcel is not found
     * @throws ParcelAlreadyDeliveredException if parcel was already delivered
     */
    public synchronized void markAsDelivered(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        parcel.markDelivered();
    }

    /**
     * Clears all parcels (primarily for testing purposes).
     */
    public synchronized void clear() {
        parcels.clear();
    }
}
