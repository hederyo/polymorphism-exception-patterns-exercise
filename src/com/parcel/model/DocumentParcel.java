package com.parcel.model;

import com.parcel.exception.InvalidParcelException;
import com.parcel.strategy.DeliveryStrategy;

/**
 * Represents a Document parcel.
 * Rules:
 * - Number of Pages: 1 to 100
 * - Maximum weight: 0.5 kg
 * - Chargeable weight = actual weight
 * - Base fee = Php 50 flat
 */
public class DocumentParcel extends Parcel {
    public static final double MAX_WEIGHT = 0.5;
    public static final int MIN_PAGES = 1;
    public static final int MAX_PAGES = 100;
    public static final double BASE_FEE = 50.0;

    private int numberOfPages;

    public DocumentParcel(String trackingNumber, String receiverName, double weight,
                          DeliveryStrategy deliveryStrategy, int numberOfPages)
            throws InvalidParcelException {
        super(trackingNumber, receiverName, weight, deliveryStrategy);
        validateDocumentFields(weight, numberOfPages);
        this.numberOfPages = numberOfPages;
    }

    private static void validateDocumentFields(double weight, int numberOfPages)
            throws InvalidParcelException {
        if (weight > MAX_WEIGHT) {
            throw new InvalidParcelException(String.format(
                    "Document weight cannot exceed %.1f kg (entered: %.2f kg)!", MAX_WEIGHT, weight));
        }
        if (numberOfPages < MIN_PAGES || numberOfPages > MAX_PAGES) {
            throw new InvalidParcelException(String.format(
                    "Number of pages must be between %d and %d (entered: %d)!", MIN_PAGES, MAX_PAGES, numberOfPages));
        }
    }

    @Override
    public double getChargeableWeight() {
        return getWeight();
    }

    @Override
    public double calculateBaseFee() {
        return BASE_FEE;
    }

    @Override
    public String getParcelType() {
        return "Document";
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) throws InvalidParcelException {
        if (numberOfPages < MIN_PAGES || numberOfPages > MAX_PAGES) {
            throw new InvalidParcelException(String.format(
                    "Number of pages must be between %d and %d (entered: %d)!", MIN_PAGES, MAX_PAGES, numberOfPages));
        }
        this.numberOfPages = numberOfPages;
    }
}
