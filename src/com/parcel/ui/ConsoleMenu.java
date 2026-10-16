package com.parcel.ui;

import com.parcel.exception.DuplicateTrackingNumberException;
import com.parcel.exception.InvalidParcelException;
import com.parcel.exception.ParcelAlreadyDeliveredException;
import com.parcel.exception.ParcelNotFoundException;
import com.parcel.exception.ServiceNotAllowedException;
import com.parcel.model.BoxParcel;
import com.parcel.model.DocumentParcel;
import com.parcel.model.FragileParcel;
import com.parcel.model.Parcel;
import com.parcel.service.ParcelManager;
import com.parcel.strategy.DeliveryServiceType;
import com.parcel.strategy.DeliveryStrategy;
import com.parcel.strategy.DeliveryStrategyFactory;

import java.util.List;
import java.util.Scanner;

/**
 * Text-based user interface for the Parcel Delivery System.
 * Cleanly structured without while(true) loops or nested if-else statements.
 */
public class ConsoleMenu {
    private final ParcelManager parcelManager;
    private final Scanner scanner;

    public ConsoleMenu(Scanner scanner) {
        this.parcelManager = ParcelManager.getInstance();
        this.scanner = scanner;
    }

    /**
     * Starts the main menu event loop.
     */
    public void start() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            String choice = readLine("Enter your choice (1-5): ").trim();
            System.out.println();

            switch (choice) {
                case "1":
                    handleAddParcel();
                    break;
                case "2":
                    handleViewAllParcels();
                    break;
                case "3":
                    handleChangeDeliveryService();
                    break;
                case "4":
                    handleMarkAsDelivered();
                    break;
                case "5":
                    System.out.println("Thank you for using the Parcel Delivery System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice! Please select an option from 1 to 5.");
                    break;
            }
            System.out.println();
        }
    }

    private void displayMainMenu() {
        System.out.println("==========================================");
        System.out.println("         PARCEL DELIVERY SYSTEM           ");
        System.out.println("==========================================");
        System.out.println("1. Add Parcel");
        System.out.println("2. View All Parcels");
        System.out.println("3. Change Delivery Service");
        System.out.println("4. Mark as Delivered");
        System.out.println("5. Exit");
        System.out.println("==========================================");
    }

    /**
     * Handles adding a new parcel with interactive input and validation.
     */
    private void handleAddParcel() {
        System.out.println("--- ADD NEW PARCEL ---");

        System.out.println("Select Parcel Type:");
        System.out.println("1. Document");
        System.out.println("2. Box");
        System.out.println("3. Fragile");

        int typeChoice = readIntInRange("Enter parcel type (1-3): ", 1, 3);
        System.out.println();

        String trackingNumber = readValidTrackingNumber();
        String receiverName = readNonEmptyString("Enter Receiver Name: ", "Receiver name cannot be empty!");

        try {
            Parcel parcel = createParcelByType(typeChoice, trackingNumber, receiverName);
            if (parcel == null) {
                System.out.println("Error: Unable to create parcel.");
                return;
            }

            parcelManager.addParcel(parcel);
            System.out.printf("Parcel added successfully! Delivery fee: Php %.2f%n", parcel.getDeliveryFee());

        } catch (DuplicateTrackingNumberException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (InvalidParcelException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }
    }

    private Parcel createParcelByType(int typeChoice, String trackingNumber, String receiverName)
            throws InvalidParcelException {
        switch (typeChoice) {
            case 1:
                return createDocumentParcel(trackingNumber, receiverName);
            case 2:
                return createBoxParcel(trackingNumber, receiverName);
            case 3:
                return createFragileParcel(trackingNumber, receiverName);
            default:
                return null;
        }
    }

    private DocumentParcel createDocumentParcel(String trackingNumber, String receiverName)
            throws InvalidParcelException {
        double weight = readDocumentWeight();
        int pages = readIntInRange("Enter Number of Pages (1 to 100): ",
                DocumentParcel.MIN_PAGES, DocumentParcel.MAX_PAGES);

        DeliveryStrategy strategy = selectDeliveryService(weight);
        return new DocumentParcel(trackingNumber, receiverName, weight, strategy, pages);
    }

    private BoxParcel createBoxParcel(String trackingNumber, String receiverName)
            throws InvalidParcelException {
        double weight = readBoxWeight(BoxParcel.MAX_WEIGHT, "Box");
        int length = readPositiveInt("Enter Length in cm: ");
        int width = readPositiveInt("Enter Width in cm: ");
        int height = readPositiveInt("Enter Height in cm: ");

        double volWeight = ((double) length * width * height) / BoxParcel.VOLUMETRIC_DIVISOR;
        double chargeableWeight = Math.ceil(Math.max(weight, volWeight));

        DeliveryStrategy strategy = selectDeliveryService(chargeableWeight);
        return new BoxParcel(trackingNumber, receiverName, weight, strategy, length, width, height);
    }

    private FragileParcel createFragileParcel(String trackingNumber, String receiverName)
            throws InvalidParcelException {
        double weight = readBoxWeight(FragileParcel.MAX_FRAGILE_WEIGHT, "Fragile");
        int length = readPositiveInt("Enter Length in cm: ");
        int width = readPositiveInt("Enter Width in cm: ");
        int height = readPositiveInt("Enter Height in cm: ");
        String note = readNonEmptyString("Enter Handling Note: ", "Handling note cannot be empty!");

        double volWeight = ((double) length * width * height) / BoxParcel.VOLUMETRIC_DIVISOR;
        double chargeableWeight = Math.ceil(Math.max(weight, volWeight));

        DeliveryStrategy strategy = selectDeliveryService(chargeableWeight);
        return new FragileParcel(trackingNumber, receiverName, weight, strategy, length, width, height, note);
    }

    /**
     * Prompts user to select delivery service, repeating until an eligible service is chosen.
     */
    private DeliveryStrategy selectDeliveryService(double chargeableWeight) {
        DeliveryStrategy strategy = null;
        boolean serviceSelected = false;

        while (!serviceSelected) {
            System.out.println("\nSelect Delivery Service:");
            System.out.println("1. Standard");
            System.out.println("2. Express");
            System.out.println("3. Same Day");

            int serviceChoice = readIntInRange("Enter choice (1-3): ", 1, 3);
            DeliveryServiceType type = mapChoiceToServiceType(serviceChoice);
            strategy = DeliveryStrategyFactory.getStrategy(type);

            try {
                strategy.validateEligibility(chargeableWeight);
                serviceSelected = true;
            } catch (ServiceNotAllowedException e) {
                System.out.println(e.getMessage());
            }
        }
        return strategy;
    }

    private DeliveryServiceType mapChoiceToServiceType(int choice) {
        switch (choice) {
            case 1:
                return DeliveryServiceType.STANDARD;
            case 2:
                return DeliveryServiceType.EXPRESS;
            case 3:
                return DeliveryServiceType.SAME_DAY;
            default:
                return DeliveryServiceType.STANDARD;
        }
    }

    /**
     * Handles displaying all parcels in a formatted table.
     */
    private void handleViewAllParcels() {
        System.out.println("--- ALL REGISTERED PARCELS ---");
        List<Parcel> parcels = parcelManager.getAllParcels();

        if (parcels.isEmpty()) {
            System.out.println("No parcels found in the system.");
            return;
        }

        String headerFormat = "| %-12s | %-10s | %-18s | %-16s | %-10s | %-14s | %-10s |%n";
        String rowFormat    = "| %-12s | %-10s | %-18s | %-16s | %-10s | Php %10.2f | %-10s |%n";
        String separator    = "+--------------+------------+--------------------+------------------+------------+----------------+------------+";

        System.out.println(separator);
        System.out.printf(headerFormat, "Tracking No.", "Type", "Receiver", "Chargeable Wt.", "Service", "Delivery Fee", "Status");
        System.out.println(separator);

        for (Parcel p : parcels) {
            String weightStr = formatWeight(p.getChargeableWeight());
            String receiver = formatReceiverName(p.getReceiverName());

            System.out.printf(rowFormat,
                    p.getTrackingNumber(),
                    p.getParcelType(),
                    receiver,
                    weightStr,
                    p.getDeliveryStrategy().getServiceName(),
                    p.getDeliveryFee(),
                    p.getStatus().getDisplayName());
        }
        System.out.println(separator);
        System.out.printf("Total Parcels: %d%n", parcels.size());
    }

    private String formatWeight(double weight) {
        if (weight % 1 == 0) {
            return String.format("%.0f kg", weight);
        }
        return String.format("%.2f kg", weight);
    }

    private String formatReceiverName(String receiver) {
        if (receiver == null) {
            return "";
        }
        if (receiver.length() > 18) {
            return receiver.substring(0, 15) + "...";
        }
        return receiver;
    }

    /**
     * Handles changing the delivery service for an existing parcel.
     */
    private void handleChangeDeliveryService() {
        System.out.println("--- CHANGE DELIVERY SERVICE ---");
        String trackingNumber = readLine("Enter Tracking Number: ").trim().toUpperCase();

        Parcel parcel;
        try {
            parcel = parcelManager.findParcel(trackingNumber);
        } catch (ParcelNotFoundException e) {
            System.out.println("Parcel not found!");
            return;
        }

        if (parcel.isDelivered()) {
            System.out.println("Parcel already delivered!");
            return;
        }

        System.out.printf("Current Service: %s (Chargeable Weight: %.2f kg)%n",
                parcel.getDeliveryStrategy().getServiceName(), parcel.getChargeableWeight());
        System.out.println("Select New Delivery Service:");
        System.out.println("1. Standard");
        System.out.println("2. Express");
        System.out.println("3. Same Day");

        int choice = readIntInRange("Enter choice (1-3): ", 1, 3);
        DeliveryServiceType newType = mapChoiceToServiceType(choice);
        DeliveryStrategy newStrategy = DeliveryStrategyFactory.getStrategy(newType);

        try {
            double newFee = parcelManager.changeDeliveryService(trackingNumber, newStrategy);
            System.out.printf("Delivery service changed! New fee: Php %.2f%n", newFee);
        } catch (ServiceNotAllowedException e) {
            System.out.println(e.getMessage());
        } catch (ParcelAlreadyDeliveredException e) {
            System.out.println("Parcel already delivered!");
        } catch (ParcelNotFoundException e) {
            System.out.println("Parcel not found!");
        } catch (InvalidParcelException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Handles marking a parcel as delivered.
     */
    private void handleMarkAsDelivered() {
        System.out.println("--- MARK AS DELIVERED ---");
        String trackingNumber = readLine("Enter Tracking Number: ").trim().toUpperCase();

        try {
            parcelManager.markAsDelivered(trackingNumber);
            System.out.println("Parcel delivered successfully!");
        } catch (ParcelNotFoundException e) {
            System.out.println("Parcel not found!");
        } catch (ParcelAlreadyDeliveredException e) {
            System.out.println("Parcel was already delivered.");
        }
    }

    // Helper input methods with no while(true) loops
    private String readValidTrackingNumber() {
        String trackingNumber = "";
        boolean valid = false;

        while (!valid) {
            trackingNumber = readLine("Enter Tracking Number (e.g. ABC0001): ").trim().toUpperCase();

            if (!Parcel.isValidTrackingFormat(trackingNumber)) {
                System.out.println("Invalid tracking number! Must be 'ABC' followed by 4 digits (e.g., ABC0001).");
                continue;
            }
            if (parcelManager.trackingNumberExists(trackingNumber)) {
                System.out.println("Tracking number already exists! Please use a unique tracking number.");
                continue;
            }
            valid = true;
        }
        return trackingNumber;
    }

    private String readNonEmptyString(String prompt, String errorMessage) {
        String text = "";
        boolean valid = false;

        while (!valid) {
            text = readLine(prompt).trim();
            if (text.isEmpty()) {
                System.out.println(errorMessage);
                continue;
            }
            valid = true;
        }
        return text;
    }

    private double readDocumentWeight() {
        double weight = 0.0;
        boolean valid = false;

        while (!valid) {
            weight = readPositiveDouble("Enter Weight in kg (max 0.5 kg): ");
            if (weight <= DocumentParcel.MAX_WEIGHT) {
                valid = true;
                continue;
            }
            System.out.printf("Document weight cannot exceed %.1f kg!%n", DocumentParcel.MAX_WEIGHT);
        }
        return weight;
    }

    private double readBoxWeight(double maxWeight, String typeName) {
        double weight = 0.0;
        boolean valid = false;

        while (!valid) {
            weight = readPositiveDouble(String.format("Enter Weight in kg (max %.0f kg): ", maxWeight));
            if (weight <= maxWeight) {
                valid = true;
                continue;
            }
            System.out.printf("%s weight cannot exceed %.0f kg!%n", typeName, maxWeight);
        }
        return weight;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int readPositiveInt(String prompt) {
        int value = 0;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Integer.parseInt(input);
                if (value <= 0) {
                    System.out.println("Value must be greater than 0!");
                    continue;
                }
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid whole number.");
            }
        }
        return value;
    }

    private int readIntInRange(String prompt, int min, int max) {
        int value = 0;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.printf("Value must be between %d and %d!%n", min, max);
                    continue;
                }
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
        return value;
    }

    private double readPositiveDouble(String prompt) {
        double value = 0.0;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Double.parseDouble(input);
                if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
                    System.out.println("Value must be greater than 0!");
                    continue;
                }
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid decimal number.");
            }
        }
        return value;
    }
}
