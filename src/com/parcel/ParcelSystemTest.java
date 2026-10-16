package com.parcel;

import com.parcel.exception.DuplicateTrackingNumberException;
import com.parcel.exception.InvalidParcelException;
import com.parcel.exception.ParcelAlreadyDeliveredException;
import com.parcel.exception.ParcelNotFoundException;
import com.parcel.exception.ServiceNotAllowedException;
import com.parcel.model.BoxParcel;
import com.parcel.model.DocumentParcel;
import com.parcel.model.FragileParcel;
import com.parcel.model.Parcel;
import com.parcel.model.ParcelStatus;
import com.parcel.service.ParcelManager;
import com.parcel.strategy.DeliveryServiceType;
import com.parcel.strategy.DeliveryStrategy;
import com.parcel.strategy.DeliveryStrategyFactory;
import com.parcel.strategy.ExpressDeliveryStrategy;
import com.parcel.strategy.SameDayDeliveryStrategy;
import com.parcel.strategy.StandardDeliveryStrategy;

/**
 * Comprehensive test runner verifying all domain rules, design patterns,
 * polymorphism, and exception handling for the Parcel Delivery System.
 */
public class ParcelSystemTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  RUNNING PARCEL DELIVERY SYSTEM TEST SUITE       ");
        System.out.println("==================================================");

        testTrackingNumberFormatValidation();
        testTrackingNumberEdgeCases();
        testDocumentParcelRules();
        testDocumentParcelEdgeCases();
        testBoxParcelRules();
        testBoxParcelEdgeCasesAndOverflow();
        testFragileParcelRulesAndPromptExample();
        testFragileParcelEdgeCases();
        testReceiverNameEdgeCases();
        testDeliveryStrategies();
        testSameDayWeightConstraint();
        testSameDayStrategyExactBoundaries();
        testDeliveryStrategyFactoryEdgeCases();
        testSingletonPattern();
        testDuplicateTrackingNumberException();
        testParcelNotFoundException();
        testParcelAlreadyDeliveredException();
        testChangeDeliveryServiceLogic();
        testParcelManagerAdvancedEdgeCases();
        testParcelPolymorphismAndContracts();

        System.out.println("==================================================");
        System.out.printf("Test Results: %d / %d PASSED%n", testsPassed, testsRun);
        System.out.println("==================================================");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("[PASS] " + testName);
            return;
        }
        System.err.println("[FAIL] " + testName);
    }

    private static void assertEquals(String testName, double expected, double actual, double epsilon) {
        testsRun++;
        if (Math.abs(expected - actual) <= epsilon) {
            testsPassed++;
            System.out.printf("[PASS] %s (Expected: %.2f, Got: %.2f)%n", testName, expected, actual);
            return;
        }
        System.err.printf("[FAIL] %s (Expected: %.2f, Got: %.2f)%n", testName, expected, actual);
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        testsRun++;
        if (expected != null && expected.equals(actual)) {
            testsPassed++;
            System.out.printf("[PASS] %s (Expected: %s, Got: %s)%n", testName, expected, actual);
            return;
        }
        System.err.printf("[FAIL] %s (Expected: %s, Got: %s)%n", testName, expected, actual);
    }

    private static void testTrackingNumberFormatValidation() {
        assertTrue("Valid tracking number ABC0001", Parcel.isValidTrackingFormat("ABC0001"));
        assertTrue("Valid tracking number ABC9999", Parcel.isValidTrackingFormat("ABC9999"));
        assertTrue("Invalid: missing prefix 1230001", !Parcel.isValidTrackingFormat("1230001"));
        assertTrue("Invalid: too few digits ABC01", !Parcel.isValidTrackingFormat("ABC01"));
        assertTrue("Invalid: too many digits ABC00001", !Parcel.isValidTrackingFormat("ABC00001"));
        assertTrue("Invalid: letters in number part ABC00A1", !Parcel.isValidTrackingFormat("ABC00A1"));
    }

    private static void testDocumentParcelRules() {
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
        try {
            DocumentParcel doc = new DocumentParcel("ABC0001", "Alice", 0.35, standard, 25);
            assertEquals("Document Base Fee is flat Php 50", 50.0, doc.calculateBaseFee(), 0.001);
            assertEquals("Document Chargeable weight equals actual weight", 0.35, doc.getChargeableWeight(), 0.001);
            assertEquals("Document Delivery fee with Standard is Php 50", 50.0, doc.getDeliveryFee(), 0.001);
            assertEquals("Document parcel type is 'Document'", "Document", doc.getParcelType());
        } catch (InvalidParcelException e) {
            assertTrue("Document creation failed unexpectedly: " + e.getMessage(), false);
        }

        // Test weight limit (> 0.5 kg should throw)
        try {
            new DocumentParcel("ABC0002", "Bob", 0.51, standard, 10);
            assertTrue("Should reject document weight > 0.5 kg", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects document weight > 0.5 kg", true);
        }

        // Test page limits (1 to 100)
        try {
            new DocumentParcel("ABC0003", "Charlie", 0.2, standard, 0);
            assertTrue("Should reject document pages < 1", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects document pages < 1", true);
        }

        try {
            new DocumentParcel("ABC0004", "David", 0.2, standard, 101);
            assertTrue("Should reject document pages > 100", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects document pages > 100", true);
        }
    }

    private static void testBoxParcelRules() {
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
        try {
            // Box: 30 x 20 x 10 cm, actual weight 2.0 kg
            // Volumetric weight: (30 * 20 * 10) / 3500 = 6000 / 3500 = 1.714 kg
            // Max(2.0, 1.714) = 2.0 -> ceil(2.0) = 2 kg
            // Base fee = 40 * 2 = 80
            BoxParcel box = new BoxParcel("ABC0005", "Emma", 2.0, standard, 30, 20, 10);
            assertEquals("Box volumetric weight", 6000.0 / 3500.0, box.getVolumetricWeight(), 0.001);
            assertEquals("Box chargeable weight (2.0 actual vs 1.714 vol -> 2 kg)", 2.0, box.getChargeableWeight(), 0.001);
            assertEquals("Box base fee is Php 40 x 2 = Php 80", 80.0, box.calculateBaseFee(), 0.001);

            // Test rounding up rule: 2.1 -> 3
            // 35 x 30 x 10 = 10500 / 3500 = 3.0 kg
            // If actual weight is 2.1 kg and volumetric is 3.0 kg -> max is 3.0 -> ceil is 3
            // If actual weight is 2.1 kg and dimensions 10x10x10 (vol 0.285) -> max is 2.1 -> ceil is 3
            BoxParcel box2 = new BoxParcel("ABC0006", "Frank", 2.1, standard, 10, 10, 10);
            assertEquals("Box rounding up (2.1 kg actual -> 3 kg chargeable)", 3.0, box2.getChargeableWeight(), 0.001);
            assertEquals("Box base fee (40 x 3 = 120)", 120.0, box2.calculateBaseFee(), 0.001);

        } catch (InvalidParcelException e) {
            assertTrue("Box creation failed unexpectedly: " + e.getMessage(), false);
        }

        // Test Box max weight (> 50 kg throws)
        try {
            new BoxParcel("ABC0007", "Grace", 50.1, standard, 10, 10, 10);
            assertTrue("Should reject Box weight > 50 kg", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects Box weight > 50 kg", true);
        }

        // Test non-positive dimensions
        try {
            new BoxParcel("ABC0008", "Henry", 5.0, standard, 0, 10, 10);
            assertTrue("Should reject Box length <= 0", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects Box dimension <= 0", true);
        }
    }

    private static void testFragileParcelRulesAndPromptExample() {
        // EXACT EXAMPLE FROM PROMPT:
        // FRAGILE, 40 x 20 x 10 cm, 1.8 kg, EXPRESS
        // Volumetric weight = 8000 / 3500 = 2.29 kg -> chargeable weight 3 kg
        // Base fee = (Php 40 x 3) + Php 100 = Php 220
        // Delivery fee = Php 220 x 1.5 = Php 330.00
        DeliveryStrategy express = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.EXPRESS);
        try {
            FragileParcel fragile = new FragileParcel("ABC0009", "Isabella", 1.8, express, 40, 20, 10, "Handle with extreme care");

            assertTrue("Fragile must extend BoxParcel", fragile instanceof BoxParcel);
            assertEquals("Fragile parcel type is 'Fragile'", "Fragile", fragile.getParcelType());
            assertEquals("Fragile volumetric weight", 8000.0 / 3500.0, fragile.getVolumetricWeight(), 0.001);
            assertEquals("Fragile chargeable weight is 3 kg", 3.0, fragile.getChargeableWeight(), 0.001);
            assertEquals("Fragile base fee is Php 220.00", 220.0, fragile.calculateBaseFee(), 0.001);
            assertEquals("Fragile delivery fee is Php 330.00", 330.00, fragile.getDeliveryFee(), 0.001);
        } catch (InvalidParcelException e) {
            assertTrue("Fragile example creation failed: " + e.getMessage(), false);
        }

        // Fragile max weight is 30 kg
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
        try {
            new FragileParcel("ABC0010", "Jack", 30.1, standard, 10, 10, 10, "Glass");
            assertTrue("Should reject Fragile weight > 30 kg", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects Fragile weight > 30 kg", true);
        }

        // Empty handling note
        try {
            new FragileParcel("ABC0011", "Kelly", 5.0, standard, 10, 10, 10, "   ");
            assertTrue("Should reject empty handling note", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects empty handling note", true);
        }
    }

    private static void testDeliveryStrategies() {
        DeliveryStrategy standard = new StandardDeliveryStrategy();
        DeliveryStrategy express = new ExpressDeliveryStrategy();
        DeliveryStrategy sameDay = new SameDayDeliveryStrategy();

        double baseFee = 200.0;
        assertEquals("Standard fee = base fee", 200.0, standard.calculateDeliveryFee(baseFee), 0.001);
        assertEquals("Express fee = base fee * 1.5", 300.0, express.calculateDeliveryFee(baseFee), 0.001);
        assertEquals("Same Day fee = (base fee * 2) + 100", 500.0, sameDay.calculateDeliveryFee(baseFee), 0.001);
    }

    private static void testSameDayWeightConstraint() {
        DeliveryStrategy sameDay = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.SAME_DAY);

        // <= 10 kg allowed
        assertTrue("Same Day allows <= 10 kg", sameDay.isEligible(10.0));
        assertTrue("Same Day allows 5 kg", sameDay.isEligible(5.0));

        // > 10 kg disallowed
        assertTrue("Same Day disallows > 10 kg", !sameDay.isEligible(10.1));
        assertTrue("Same Day disallows 15 kg", !sameDay.isEligible(15.0));

        try {
            sameDay.validateEligibility(10.1);
            assertTrue("Same Day should throw ServiceNotAllowedException for > 10 kg", false);
        } catch (ServiceNotAllowedException e) {
            assertEquals("Same Day restriction message",
                    "Same Day delivery is only for parcels up to 10 kg!", e.getMessage());
        }
    }

    private static void testSingletonPattern() {
        ParcelManager instance1 = ParcelManager.getInstance();
        ParcelManager instance2 = ParcelManager.getInstance();
        assertTrue("ParcelManager returns same instance (Singleton)", instance1 == instance2);
    }

    private static void testDuplicateTrackingNumberException() {
        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();

        try {
            DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
            DocumentParcel p1 = new DocumentParcel("ABC0001", "Alice", 0.2, standard, 10);
            DocumentParcel p2 = new DocumentParcel("ABC0001", "Bob", 0.3, standard, 15);

            manager.addParcel(p1);
            manager.addParcel(p2);
            assertTrue("Should throw DuplicateTrackingNumberException on second add", false);
        } catch (DuplicateTrackingNumberException e) {
            assertTrue("Duplicate tracking number correctly caught", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testParcelNotFoundException() {
        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();

        try {
            manager.findParcel("ABC9999");
            assertTrue("Should throw ParcelNotFoundException for unknown tracking number", false);
        } catch (ParcelNotFoundException e) {
            assertTrue("ParcelNotFoundException correctly caught", true);
        }
    }

    private static void testParcelAlreadyDeliveredException() {
        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();

        try {
            DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
            DocumentParcel doc = new DocumentParcel("ABC0001", "Alice", 0.2, standard, 10);
            manager.addParcel(doc);

            // Mark delivered
            manager.markAsDelivered("ABC0001");
            assertEquals("Status is DELIVERED", ParcelStatus.DELIVERED, doc.getStatus());

            // Marking again should throw
            try {
                manager.markAsDelivered("ABC0001");
                assertTrue("Should throw ParcelAlreadyDeliveredException on second mark", false);
            } catch (ParcelAlreadyDeliveredException e) {
                assertTrue("ParcelAlreadyDeliveredException correctly thrown when marking again", true);
            }

            // Changing service on delivered parcel should throw
            try {
                DeliveryStrategy express = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.EXPRESS);
                manager.changeDeliveryService("ABC0001", express);
                assertTrue("Should throw ParcelAlreadyDeliveredException when changing service of delivered parcel", false);
            } catch (ParcelAlreadyDeliveredException e) {
                assertTrue("ParcelAlreadyDeliveredException correctly thrown when changing service of delivered parcel", true);
            }

        } catch (Exception e) {
            assertTrue("Unexpected exception in delivered test: " + e.getMessage(), false);
        }
    }

    private static void testChangeDeliveryServiceLogic() {
        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();

        try {
            DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
            DeliveryStrategy express = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.EXPRESS);
            DeliveryStrategy sameDay = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.SAME_DAY);

            // Box: 50x40x35 = 70000 / 3500 = 20 kg chargeable weight
            BoxParcel heavyBox = new BoxParcel("ABC0002", "Heavy Parcel", 5.0, standard, 50, 40, 35);
            manager.addParcel(heavyBox);

            assertEquals("Heavy box chargeable weight is 20 kg", 20.0, heavyBox.getChargeableWeight(), 0.001);

            // Changing to Express should succeed (Base fee = 20 * 40 = 800, Express = 800 * 1.5 = 1200)
            double newFee = manager.changeDeliveryService("ABC0002", express);
            assertEquals("Express fee for 20 kg box", 1200.0, newFee, 0.001);
            assertEquals("Strategy changed to Express", DeliveryServiceType.EXPRESS, heavyBox.getDeliveryStrategy().getServiceType());

            // Attempting to change to Same Day on 20 kg parcel should fail
            try {
                manager.changeDeliveryService("ABC0002", sameDay);
                assertTrue("Should reject Same Day for 20 kg parcel", false);
            } catch (ServiceNotAllowedException e) {
                assertTrue("Correctly blocked Same Day for > 10 kg parcel", true);
                assertEquals("Retained Express strategy", DeliveryServiceType.EXPRESS, heavyBox.getDeliveryStrategy().getServiceType());
            }

        } catch (Exception e) {
            assertTrue("Unexpected exception in change service test: " + e.getMessage(), false);
        }
    }

    private static void testTrackingNumberEdgeCases() {
        // Lowercase normalization
        assertTrue("Valid lowercase tracking number abc0001", Parcel.isValidTrackingFormat("abc0001"));
        assertTrue("Valid mixed case tracking number aBc9999", Parcel.isValidTrackingFormat("aBc9999"));
        assertTrue("Valid padded tracking number '  ABC1234  '", Parcel.isValidTrackingFormat("  ABC1234  "));
        assertTrue("Valid lowest digits tracking number ABC0000", Parcel.isValidTrackingFormat("ABC0000"));

        // Invalid tracking numbers
        assertTrue("Invalid: null tracking number", !Parcel.isValidTrackingFormat(null));
        assertTrue("Invalid: empty tracking number", !Parcel.isValidTrackingFormat(""));
        assertTrue("Invalid: whitespace-only tracking number", !Parcel.isValidTrackingFormat("   "));
        assertTrue("Invalid: 3 digits ABC123", !Parcel.isValidTrackingFormat("ABC123"));
        assertTrue("Invalid: 5 digits ABC12345", !Parcel.isValidTrackingFormat("ABC12345"));
        assertTrue("Invalid: wrong prefix XYZ1234", !Parcel.isValidTrackingFormat("XYZ1234"));
        assertTrue("Invalid: all digits 1234567", !Parcel.isValidTrackingFormat("1234567"));
        assertTrue("Invalid: hyphenated ABC-1234", !Parcel.isValidTrackingFormat("ABC-1234"));
        assertTrue("Invalid: space in tracking number ABC 1234", !Parcel.isValidTrackingFormat("ABC 1234"));

        // Constructor rejection for invalid tracking numbers
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
        try {
            new DocumentParcel("XYZ1234", "Alice", 0.3, standard, 10);
            assertTrue("Should reject invalid tracking prefix in constructor", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects invalid tracking prefix in constructor", true);
        }

        try {
            new DocumentParcel(null, "Alice", 0.3, standard, 10);
            assertTrue("Should reject null tracking in constructor", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects null tracking in constructor", true);
        }
    }

    private static void testDocumentParcelEdgeCases() {
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
        DeliveryStrategy express = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.EXPRESS);
        DeliveryStrategy sameDay = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.SAME_DAY);

        // Exact weight boundary 0.5 kg
        try {
            DocumentParcel docMax = new DocumentParcel("ABC1001", "Bob", 0.5, standard, 50);
            assertEquals("Document exact max weight 0.5 kg accepted", 0.5, docMax.getWeight(), 0.001);
        } catch (InvalidParcelException e) {
            assertTrue("Document exact max weight 0.5 kg failed unexpectedly", false);
        }

        // Just over weight boundary 0.5001 kg
        try {
            new DocumentParcel("ABC1002", "Bob", 0.5001, standard, 50);
            assertTrue("Should reject document weight 0.5001 kg", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects document weight 0.5001 kg", true);
        }

        // Extremely small positive weight 0.0001 kg
        try {
            DocumentParcel docTiny = new DocumentParcel("ABC1003", "Bob", 0.0001, standard, 50);
            assertEquals("Document tiny weight 0.0001 kg accepted", 0.0001, docTiny.getWeight(), 0.00001);
        } catch (InvalidParcelException e) {
            assertTrue("Document tiny weight failed unexpectedly", false);
        }

        // Weight <= 0 kg
        try {
            new DocumentParcel("ABC1004", "Bob", 0.0, standard, 50);
            assertTrue("Should reject 0 kg document weight", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects 0 kg document weight", true);
        }

        try {
            new DocumentParcel("ABC1005", "Bob", -0.2, standard, 50);
            assertTrue("Should reject negative document weight", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects negative document weight", true);
        }

        // NaN and Infinity weight
        try {
            new DocumentParcel("ABC1006", "Bob", Double.NaN, standard, 50);
            assertTrue("Should reject NaN document weight", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects NaN document weight", true);
        }

        try {
            new DocumentParcel("ABC1007", "Bob", Double.POSITIVE_INFINITY, standard, 50);
            assertTrue("Should reject Infinity document weight", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects Infinity document weight", true);
        }

        // Exact page boundaries: 1 and 100
        try {
            DocumentParcel docMinPages = new DocumentParcel("ABC1008", "Bob", 0.2, standard, 1);
            assertEquals("Document exact min pages 1 accepted", 1, docMinPages.getNumberOfPages());
            DocumentParcel docMaxPages = new DocumentParcel("ABC1009", "Bob", 0.2, standard, 100);
            assertEquals("Document exact max pages 100 accepted", 100, docMaxPages.getNumberOfPages());

            // setNumberOfPages edge cases
            docMinPages.setNumberOfPages(100);
            assertEquals("setNumberOfPages(100) accepted", 100, docMinPages.getNumberOfPages());

            try {
                docMinPages.setNumberOfPages(0);
                assertTrue("setNumberOfPages(0) should fail", false);
            } catch (InvalidParcelException e) {
                assertTrue("setNumberOfPages(0) rejected", true);
            }

            try {
                docMinPages.setNumberOfPages(101);
                assertTrue("setNumberOfPages(101) should fail", false);
            } catch (InvalidParcelException e) {
                assertTrue("setNumberOfPages(101) rejected", true);
            }

        } catch (InvalidParcelException e) {
            assertTrue("Document page boundary creation failed unexpectedly", false);
        }

        // Page boundary violations: 0, 101, -5
        try {
            new DocumentParcel("ABC1010", "Bob", 0.2, standard, 0);
            assertTrue("Should reject 0 pages", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects 0 pages", true);
        }

        try {
            new DocumentParcel("ABC1011", "Bob", 0.2, standard, 101);
            assertTrue("Should reject 101 pages", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects 101 pages", true);
        }

        try {
            new DocumentParcel("ABC1012", "Bob", 0.2, standard, -5);
            assertTrue("Should reject negative pages", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects negative pages", true);
        }

        // Document with Express and Same Day service calculations
        try {
            DocumentParcel docExpress = new DocumentParcel("ABC1013", "Bob", 0.3, express, 20);
            assertEquals("Document Express fee (50 * 1.5 = 75.0)", 75.0, docExpress.getDeliveryFee(), 0.001);

            DocumentParcel docSameDay = new DocumentParcel("ABC1014", "Bob", 0.3, sameDay, 20);
            assertEquals("Document Same Day fee ((50 * 2) + 100 = 200.0)", 200.0, docSameDay.getDeliveryFee(), 0.001);
        } catch (InvalidParcelException e) {
            assertTrue("Document service fee calculation failed unexpectedly", false);
        }
    }

    private static void testBoxParcelEdgeCasesAndOverflow() {
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);

        // Exact weight boundary 50.0 kg
        try {
            BoxParcel boxMax = new BoxParcel("ABC2001", "Charlie", 50.0, standard, 10, 10, 10);
            assertEquals("Box exact max weight 50.0 kg accepted", 50.0, boxMax.getWeight(), 0.001);
        } catch (InvalidParcelException e) {
            assertTrue("Box exact max weight 50.0 kg failed unexpectedly", false);
        }

        // Weight 50.0001 kg
        try {
            new BoxParcel("ABC2002", "Charlie", 50.0001, standard, 10, 10, 10);
            assertTrue("Should reject box weight 50.0001 kg", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects box weight 50.0001 kg", true);
        }

        // Weight <= 0, NaN, Infinity
        try {
            new BoxParcel("ABC2003", "Charlie", 0.0, standard, 10, 10, 10);
            assertTrue("Should reject 0 kg box weight", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects 0 kg box weight", true);
        }

        try {
            new BoxParcel("ABC2004", "Charlie", -1.0, standard, 10, 10, 10);
            assertTrue("Should reject negative box weight", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects negative box weight", true);
        }

        try {
            new BoxParcel("ABC2005", "Charlie", Double.NaN, standard, 10, 10, 10);
            assertTrue("Should reject NaN box weight", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects NaN box weight", true);
        }

        // Dimension boundaries: 1x1x1 cm
        try {
            BoxParcel minBox = new BoxParcel("ABC2006", "Charlie", 1.0, standard, 1, 1, 1);
            assertEquals("1x1x1 cm box length accepted", 1, minBox.getLength());
            assertEquals("1x1x1 cm box chargeable weight is 1.0 kg", 1.0, minBox.getChargeableWeight(), 0.001);

            // setDimensions test
            minBox.setDimensions(20, 20, 20);
            assertEquals("setDimensions valid length", 20, minBox.getLength());

            try {
                minBox.setDimensions(0, 20, 20);
                assertTrue("setDimensions length 0 should fail", false);
            } catch (InvalidParcelException e) {
                assertTrue("setDimensions length 0 rejected", true);
            }

            try {
                minBox.setDimensions(20, -1, 20);
                assertTrue("setDimensions width -1 should fail", false);
            } catch (InvalidParcelException e) {
                assertTrue("setDimensions width -1 rejected", true);
            }

        } catch (InvalidParcelException e) {
            assertTrue("Box 1x1x1 cm failed unexpectedly", false);
        }

        // Non-positive dimensions rejection
        try {
            new BoxParcel("ABC2007", "Charlie", 1.0, standard, 0, 10, 10);
            assertTrue("Should reject length 0", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects length 0", true);
        }

        try {
            new BoxParcel("ABC2008", "Charlie", 1.0, standard, 10, 0, 10);
            assertTrue("Should reject width 0", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects width 0", true);
        }

        try {
            new BoxParcel("ABC2009", "Charlie", 1.0, standard, 10, 10, 0);
            assertTrue("Should reject height 0", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects height 0", true);
        }

        try {
            new BoxParcel("ABC2010", "Charlie", 1.0, standard, -5, 10, 10);
            assertTrue("Should reject negative dimension", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects negative dimension", true);
        }

        // Large dimensions integer overflow prevention check (2000 x 2000 x 2000)
        try {
            BoxParcel largeBox = new BoxParcel("ABC2011", "Charlie", 10.0, standard, 2000, 2000, 2000);
            double expectedVol = 8000000000.0 / 3500.0;
            assertTrue("Large dimensions volumetric weight positive (> 0)", largeBox.getVolumetricWeight() > 0);
            assertEquals("Large dimensions volumetric weight exact", expectedVol, largeBox.getVolumetricWeight(), 0.01);
            assertEquals("Large dimensions chargeable weight ceil", Math.ceil(expectedVol), largeBox.getChargeableWeight(), 0.01);
        } catch (InvalidParcelException e) {
            assertTrue("Large dimensions box failed unexpectedly", false);
        }

        // Chargeable weight ceiling / tie-break rules:
        try {
            // Whole number actual weight: 4.0 kg, vol = 1.0 kg -> chargeable 4.0 kg
            BoxParcel wholeBox = new BoxParcel("ABC2012", "Charlie", 4.0, standard, 10, 10, 10);
            assertEquals("Exact whole actual weight not rounded up", 4.0, wholeBox.getChargeableWeight(), 0.001);

            // Fractional actual weight: 4.0001 kg, vol = 1.0 kg -> chargeable 5.0 kg
            BoxParcel fracBox = new BoxParcel("ABC2013", "Charlie", 4.0001, standard, 10, 10, 10);
            assertEquals("Fractional actual weight rounded up to next kg", 5.0, fracBox.getChargeableWeight(), 0.001);

            // Exact integer volumetric weight: (35 * 20 * 5) = 3500 / 3500 = 1.0 kg, actual = 0.5 -> 1.0 kg
            BoxParcel wholeVolBox = new BoxParcel("ABC2014", "Charlie", 0.5, standard, 35, 20, 5);
            assertEquals("Exact integer volumetric weight", 1.0, wholeVolBox.getVolumetricWeight(), 0.001);
            assertEquals("Exact integer volumetric chargeable weight", 1.0, wholeVolBox.getChargeableWeight(), 0.001);

            // Fractional volumetric weight: 3501 / 3500 = 1.00028 kg -> 2.0 kg
            BoxParcel fracVolBox = new BoxParcel("ABC2015", "Charlie", 0.5, standard, 3501, 1, 1);
            assertEquals("Fractional volumetric chargeable weight rounded up", 2.0, fracVolBox.getChargeableWeight(), 0.001);
        } catch (InvalidParcelException e) {
            assertTrue("Ceiling tie-break test failed unexpectedly", false);
        }
    }

    private static void testFragileParcelEdgeCases() {
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
        DeliveryStrategy express = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.EXPRESS);
        DeliveryStrategy sameDay = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.SAME_DAY);

        // Exact weight boundary 30.0 kg
        try {
            FragileParcel fragileMax = new FragileParcel("ABC3001", "David", 30.0, standard, 10, 10, 10, "Glass Vase");
            assertEquals("Fragile exact max weight 30.0 kg accepted", 30.0, fragileMax.getWeight(), 0.001);
        } catch (InvalidParcelException e) {
            assertTrue("Fragile exact max weight failed unexpectedly", false);
        }

        // Just over weight boundary 30.0001 kg
        try {
            new FragileParcel("ABC3002", "David", 30.0001, standard, 10, 10, 10, "Glass");
            assertTrue("Should reject fragile weight 30.0001 kg", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects fragile weight 30.0001 kg", true);
        }

        // Weight 40.0 kg (valid for Box, but invalid for Fragile)
        try {
            new FragileParcel("ABC3003", "David", 40.0, standard, 10, 10, 10, "Glass");
            assertTrue("Should reject 40 kg for Fragile", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects 40 kg for Fragile", true);
        }

        // Handling note null, empty, whitespace
        try {
            new FragileParcel("ABC3004", "David", 5.0, standard, 10, 10, 10, null);
            assertTrue("Should reject null handling note", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects null handling note", true);
        }

        try {
            new FragileParcel("ABC3005", "David", 5.0, standard, 10, 10, 10, "");
            assertTrue("Should reject empty handling note", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects empty handling note", true);
        }

        try {
            new FragileParcel("ABC3006", "David", 5.0, standard, 10, 10, 10, "   ");
            assertTrue("Should reject whitespace handling note", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects whitespace handling note", true);
        }

        // Handling note trimming and setter
        try {
            FragileParcel fragileTrim = new FragileParcel("ABC3007", "David", 5.0, standard, 10, 10, 10, "  Handle with care  ");
            assertEquals("Handling note trimmed", "Handle with care", fragileTrim.getHandlingNote());

            fragileTrim.setHandlingNote("Do not drop");
            assertEquals("setHandlingNote valid", "Do not drop", fragileTrim.getHandlingNote());

            try {
                fragileTrim.setHandlingNote(null);
                assertTrue("setHandlingNote(null) should fail", false);
            } catch (InvalidParcelException e) {
                assertTrue("setHandlingNote(null) rejected", true);
            }

            try {
                fragileTrim.setHandlingNote("  ");
                assertTrue("setHandlingNote empty should fail", false);
            } catch (InvalidParcelException e) {
                assertTrue("setHandlingNote empty rejected", true);
            }
        } catch (InvalidParcelException e) {
            assertTrue("Fragile trimming test failed unexpectedly", false);
        }

        // Fragile fee calculation across services (1 kg chargeable: base = 40 + 100 = 140)
        try {
            FragileParcel fragStd = new FragileParcel("ABC3008", "David", 1.0, standard, 10, 10, 10, "Vase");
            assertEquals("Fragile 1 kg base fee is Php 140", 140.0, fragStd.calculateBaseFee(), 0.001);
            assertEquals("Fragile 1 kg Standard fee is Php 140", 140.0, fragStd.getDeliveryFee(), 0.001);

            FragileParcel fragExp = new FragileParcel("ABC3009", "David", 1.0, express, 10, 10, 10, "Vase");
            assertEquals("Fragile 1 kg Express fee is Php 210 (140 * 1.5)", 210.0, fragExp.getDeliveryFee(), 0.001);

            FragileParcel fragSame = new FragileParcel("ABC3010", "David", 1.0, sameDay, 10, 10, 10, "Vase");
            assertEquals("Fragile 1 kg Same Day fee is Php 380 ((140 * 2) + 100)", 380.0, fragSame.getDeliveryFee(), 0.001);
        } catch (InvalidParcelException e) {
            assertTrue("Fragile fee calculation failed unexpectedly", false);
        }
    }

    private static void testReceiverNameEdgeCases() {
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);

        // Null, empty, whitespace receiver
        try {
            new DocumentParcel("ABC4001", null, 0.2, standard, 10);
            assertTrue("Should reject null receiver", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects null receiver name", true);
        }

        try {
            new DocumentParcel("ABC4002", "", 0.2, standard, 10);
            assertTrue("Should reject empty receiver", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects empty receiver name", true);
        }

        try {
            new DocumentParcel("ABC4003", "   ", 0.2, standard, 10);
            assertTrue("Should reject whitespace receiver", false);
        } catch (InvalidParcelException e) {
            assertTrue("Rejects whitespace receiver name", true);
        }

        // Receiver trimming and setter
        try {
            DocumentParcel docTrim = new DocumentParcel("ABC4004", "  John Doe  ", 0.2, standard, 10);
            assertEquals("Receiver name trimmed", "John Doe", docTrim.getReceiverName());

            docTrim.setReceiverName("Jane Doe");
            assertEquals("setReceiverName valid", "Jane Doe", docTrim.getReceiverName());

            try {
                docTrim.setReceiverName(null);
                assertTrue("setReceiverName(null) should fail", false);
            } catch (InvalidParcelException e) {
                assertTrue("setReceiverName(null) rejected", true);
            }

            try {
                docTrim.setReceiverName("   ");
                assertTrue("setReceiverName whitespace should fail", false);
            } catch (InvalidParcelException e) {
                assertTrue("setReceiverName whitespace rejected", true);
            }
        } catch (InvalidParcelException e) {
            assertTrue("Receiver trimming test failed unexpectedly", false);
        }
    }

    private static void testSameDayStrategyExactBoundaries() {
        DeliveryStrategy sameDay = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.SAME_DAY);
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);
        DeliveryStrategy express = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.EXPRESS);

        // Exact 10.0 kg boundary
        assertTrue("Exact 10.0 kg is eligible for Same Day", sameDay.isEligible(10.0));
        assertTrue("9.9999 kg is eligible for Same Day", sameDay.isEligible(9.9999));
        assertTrue("10.0001 kg is NOT eligible for Same Day", !sameDay.isEligible(10.0001));

        try {
            sameDay.validateEligibility(10.0);
            assertTrue("Same Day validateEligibility passes for exact 10.0 kg", true);
        } catch (ServiceNotAllowedException e) {
            assertTrue("Same Day validateEligibility failed unexpectedly for 10.0 kg", false);
        }

        try {
            sameDay.validateEligibility(10.0001);
            assertTrue("Same Day should throw for 10.0001 kg", false);
        } catch (ServiceNotAllowedException e) {
            assertTrue("Same Day throws ServiceNotAllowedException for 10.0001 kg", true);
        }

        // Standard and Express accept any weight
        assertTrue("Standard allows 100 kg", standard.isEligible(100.0));
        assertTrue("Express allows 100 kg", express.isEligible(100.0));

        try {
            standard.validateEligibility(100.0);
            assertTrue("Standard validateEligibility passes for 100 kg", true);
            express.validateEligibility(100.0);
            assertTrue("Express validateEligibility passes for 100 kg", true);
        } catch (ServiceNotAllowedException e) {
            assertTrue("Standard/Express failed unexpectedly: " + e.getMessage(), false);
        }
    }

    private static void testDeliveryStrategyFactoryEdgeCases() {
        // Null service type
        try {
            DeliveryStrategyFactory.getStrategy(null);
            assertTrue("Factory should throw on null type", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Factory throws IllegalArgumentException on null type", true);
        }

        // Flyweight caching (identical instances returned)
        assertTrue("Factory caches STANDARD strategy instance",
                DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD) == DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD));
        assertTrue("Factory caches EXPRESS strategy instance",
                DeliveryStrategyFactory.getStrategy(DeliveryServiceType.EXPRESS) == DeliveryStrategyFactory.getStrategy(DeliveryServiceType.EXPRESS));
        assertTrue("Factory caches SAME_DAY strategy instance",
                DeliveryStrategyFactory.getStrategy(DeliveryServiceType.SAME_DAY) == DeliveryStrategyFactory.getStrategy(DeliveryServiceType.SAME_DAY));
    }

    private static void testParcelManagerAdvancedEdgeCases() {
        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();

        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);

        // Null argument handling in addParcel
        try {
            manager.addParcel(null);
            assertTrue("addParcel(null) should throw InvalidParcelException", false);
        } catch (InvalidParcelException e) {
            assertTrue("addParcel(null) correctly throws InvalidParcelException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e.getMessage(), false);
        }

        // Null argument in findParcel
        try {
            manager.findParcel(null);
            assertTrue("findParcel(null) should throw ParcelNotFoundException", false);
        } catch (ParcelNotFoundException e) {
            assertTrue("findParcel(null) correctly throws ParcelNotFoundException", true);
        }

        // Case-insensitive lookup and trackingNumberExists
        try {
            DocumentParcel doc = new DocumentParcel("ABC5001", "Alice", 0.2, standard, 10);
            manager.addParcel(doc);

            assertEquals("findParcel case-insensitive lookup", doc, manager.findParcel("abc5001"));
            assertTrue("trackingNumberExists case-insensitive", manager.trackingNumberExists("abc5001"));
            assertTrue("trackingNumberExists(null) returns false", !manager.trackingNumberExists(null));

            // Immutability of getAllParcels()
            try {
                manager.getAllParcels().clear();
                assertTrue("getAllParcels() should be unmodifiable", false);
            } catch (UnsupportedOperationException e) {
                assertTrue("getAllParcels() is unmodifiable and protected", true);
            }

            // Parcel count updates
            assertEquals("Parcel count is 1", 1, manager.getParcelCount());
            manager.clear();
            assertEquals("Parcel count after clear is 0", 0, manager.getParcelCount());

            // changeDeliveryService with non-existent parcel
            try {
                manager.changeDeliveryService("ABC9999", standard);
                assertTrue("changeDeliveryService unknown tracking should throw", false);
            } catch (ParcelNotFoundException e) {
                assertTrue("changeDeliveryService throws ParcelNotFoundException for unknown parcel", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception: " + e.getMessage(), false);
            }

            // changeDeliveryService with null strategy
            manager.addParcel(doc);
            try {
                manager.changeDeliveryService("ABC5001", null);
                assertTrue("changeDeliveryService null strategy should throw", false);
            } catch (InvalidParcelException e) {
                assertTrue("changeDeliveryService throws InvalidParcelException for null strategy", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception: " + e.getMessage(), false);
            }

            // markAsDelivered with non-existent parcel
            try {
                manager.markAsDelivered("ABC9999");
                assertTrue("markAsDelivered unknown tracking should throw", false);
            } catch (ParcelNotFoundException e) {
                assertTrue("markAsDelivered throws ParcelNotFoundException for unknown parcel", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception: " + e.getMessage(), false);
            }

        } catch (Exception e) {
            assertTrue("Unexpected exception in advanced manager edge cases: " + e.getMessage(), false);
        }
    }

    private static void testParcelPolymorphismAndContracts() {
        DeliveryStrategy standard = DeliveryStrategyFactory.getStrategy(DeliveryServiceType.STANDARD);

        try {
            DocumentParcel p1 = new DocumentParcel("ABC6001", "Alice", 0.2, standard, 10);
            DocumentParcel p2 = new DocumentParcel("ABC6001", "Bob", 0.4, standard, 20);

            // Reflexive
            assertTrue("Parcel equals itself (reflexive)", p1.equals(p1));

            // Null and other class
            assertTrue("Parcel does not equal null", !p1.equals(null));
            assertTrue("Parcel does not equal non-parcel object", !p1.equals("ABC6001"));

            // Same tracking number equality and hashCode
            assertEquals("Parcels with same tracking number are equal", p1, p2);
            assertEquals("Parcels with same tracking number have matching hashCode", p1.hashCode(), p2.hashCode());

            // toString() contract
            String str = p1.toString();
            assertTrue("toString() contains tracking number", str.contains("ABC6001"));
            assertTrue("toString() contains receiver name", str.contains("Alice"));
            assertTrue("toString() contains parcel type", str.contains("Document"));

        } catch (InvalidParcelException e) {
            assertTrue("Polymorphism test failed unexpectedly: " + e.getMessage(), false);
        }
    }
}

