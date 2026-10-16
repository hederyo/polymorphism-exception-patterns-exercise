# Parcel Delivery System

> **Repository Name:** `polymorphism-exception-patterns-exercise`  
> **Language:** Java (JDK 17+)  
> **Architecture:** Object-Oriented Design demonstrating **Polymorphism**, **Custom Exception Handling**, the **Singleton Pattern**, and the **Strategy Pattern**.

---

## 📌 Project Overview

The **Parcel Delivery System** is a modular, console-driven Java application designed to manage, calculate fees for, and track different types of delivery packages. The system strictly adheres to object-oriented software engineering principles, incorporating clean separation of concerns, robust input validation, and industry-standard design patterns.

---

## 🏗️ Architecture & Design Patterns

### 1. Polymorphism (Parcel Hierarchy)
An abstract base class `Parcel` models core parcel attributes and defines abstract methods implemented polymorphically by specialized parcel types:

```
               ┌───────────────────────┐
               │    abstract Parcel    │
               └───────────┬───────────┘
                           │
             ┌─────────────┴─────────────┐
             │                           │
  ┌───────────────────────┐   ┌───────────────────────┐
  │    DocumentParcel     │   │       BoxParcel       │
  └───────────────────────┘   └───────────┬───────────┘
                                          │ extends
                              ┌───────────┴───────────┐
                              │     FragileParcel     │
                              └───────────────────────┘
```

- **`Parcel` (abstract):**
  - Defines polymorphic contracts:
    - `abstract double calculateBaseFee()`
    - `abstract double getChargeableWeight()`
    - `abstract String getParcelType()`
  - Encapsulates common attributes: `trackingNumber`, `receiverName`, `weight`, `deliveryStrategy`, and `status`.
  - Calculates total fee polymorphically via its assigned `DeliveryStrategy`:  
    `getDeliveryFee() -> deliveryStrategy.calculateDeliveryFee(calculateBaseFee())`
- **`DocumentParcel`:**
  - Additional attribute: `numberOfPages` (1 to 100).
  - Maximum weight: `0.5 kg`.
  - Chargeable weight: equals actual weight.
  - Base fee: flat `Php 50.00`.
- **`BoxParcel`:**
  - Additional attributes: `length`, `width`, `height` in cm (integers > 0).
  - Maximum weight: `50.0 kg`.
  - Volumetric weight: `(length * width * height) / 3500.0`.
  - Chargeable weight: `Math.ceil(Math.max(weight, volumetricWeight))` (rounded UP to the next whole kg).
  - Base fee: `Php 40.00 * chargeableWeight`.
- **`FragileParcel` (Extends `BoxParcel`):**
  - Additional attribute: `handlingNote` (non-empty string).
  - Maximum weight: `30.0 kg`.
  - Chargeable weight: inherits Box calculation rule.
  - Base fee: `super.calculateBaseFee() + Php 100.00` handling fee.

---

### 2. Strategy Design Pattern (Delivery Pricing)
Pricing calculations and eligibility constraints for different service tiers are decoupled from parcel models using the **Strategy Pattern**:

```
                  ┌────────────────────────┐
                  │   «interface»          │
                  │    DeliveryStrategy    │
                  └───────────┬────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│StandardDelivery │  │ ExpressDelivery │  │ SameDayDelivery │
│    Strategy     │  │    Strategy     │  │    Strategy     │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

- **`DeliveryStrategy` (Interface):**
  - `double calculateDeliveryFee(double baseFee)`
  - `void validateEligibility(double chargeableWeight) throws ServiceNotAllowedException`
  - `boolean isEligible(double chargeableWeight)`
- **`StandardDeliveryStrategy`:**
  - Delivery fee = `Base fee`.
  - Allowed for all chargeable weights.
- **`ExpressDeliveryStrategy`:**
  - Delivery fee = `Base fee * 1.5`.
  - Allowed for all chargeable weights.
- **`SameDayDeliveryStrategy`:**
  - Delivery fee = `(Base fee * 2.0) + Php 100.00`.
  - Restricted to parcels with **chargeable weight ≤ 10 kg**. Throws `ServiceNotAllowedException` if exceeded.
- **`DeliveryStrategyFactory`:**
  - Provides centralized instantiation and cached access to strategy implementations.

---

### 3. Singleton Design Pattern (`ParcelManager`)
`ParcelManager` manages the central in-memory repository of all parcels, ensuring a single coordinated point of access:
- **Private Constructor:** Guarantees no external instantiation.
- **Bill Pugh Singleton Holder:** Employs the initialization-on-demand holder idiom (`InstanceHolder`) providing thread-safe, lazy instantiation guaranteed by the JVM class loader without synchronization overhead or nested conditional checks.
- **Synchronized Domain Operations:**
  - `addParcel(Parcel parcel)`
  - `findParcel(String trackingNumber)`
  - `changeDeliveryService(String trackingNumber, DeliveryStrategy newStrategy)`
  - `markAsDelivered(String trackingNumber)`
  - `getAllParcels()`

---

### 4. Clean Control Flow (Zero `while(true)` & Zero Nested `if-else`)
The entire codebase strictly follows clean code and structural best practices:
- **No `while(true)` Loops:** All input and event loops are controlled by explicit boolean flags (`while (!valid)`, `while (!serviceSelected)`, `while (running)`).
- **No Nested `if-else`:** All conditional flows use single-level guard clauses, early returns, switch statements, and dedicated factory methods—completely eliminating nested branching.

---

### 5. Custom Exception Handling Hierarchy
Domain business logic invariants are protected through a clean checked-exception hierarchy:

- **`ParcelException`:** Base checked exception.
  - **`DuplicateTrackingNumberException`:** Thrown when registering a tracking number that already exists.
  - **`ParcelNotFoundException`:** Thrown when querying a tracking number absent from the system.
  - **`ParcelAlreadyDeliveredException`:** Thrown when attempting to deliver an already delivered package or altering its delivery service.
  - **`InvalidParcelException`:** Thrown when inputs violate validation rules (e.g., negative dimensions, weight exceeding tier limit, invalid pages, invalid tracking number regex `^ABC\d{4}$`).
  - **`ServiceNotAllowedException`:** Thrown when selecting Same Day delivery for parcels exceeding 10 kg.

---

## 📊 Fee Calculation Example (from Specification)

```
Parcel Type:        FRAGILE (Extends BOX)
Dimensions:         40 cm x 20 cm x 10 cm
Actual Weight:      1.8 kg
Delivery Service:   EXPRESS

Step 1: Volumetric Weight = (40 x 20 x 10) / 3500 = 8000 / 3500 = 2.29 kg
Step 2: Chargeable Weight = ceil(max(1.8, 2.29)) = 3 kg
Step 3: Box Base Fee      = Php 40 x 3 = Php 120.00
Step 4: Fragile Base Fee  = Php 120.00 + Php 100.00 handling fee = Php 220.00
Step 5: Delivery Fee      = Php 220.00 x 1.5 = Php 330.00
```

---

## 📋 Menu Operations

1. **Add Parcel:**
   - Selects type (`Document`, `Box`, `Fragile`).
   - Validates tracking number (unique `ABC` + 4 digits).
   - Validates receiver name, positive weight, and type-specific attributes.
   - Selects delivery service; prompts again with `"Same Day delivery is only for parcels up to 10 kg!"` if weight exceeds 10 kg.
   - Displays `"Parcel added successfully! Delivery fee: Php <fee>"`.
2. **View All Parcels:**
   - Renders a clean formatted table displaying Tracking Number, Type, Receiver, Chargeable Weight, Service, Delivery Fee, and Status.
3. **Change Delivery Service:**
   - Prompts for Tracking Number.
   - Validates existence (`"Parcel not found!"`) and status (`"Parcel already delivered!"`).
   - If pending: updates service or warns `"Same Day delivery is only for parcels up to 10 kg!"` while retaining existing service.
4. **Mark as Delivered:**
   - Marks a pending parcel as delivered (`"Parcel delivered successfully!"`).
   - If already delivered, notifies: `"Parcel was already delivered."`.
   - If not found, notifies: `"Parcel not found!"`.
5. **Exit:**
   - Graceful termination message.

---

## 🚀 How to Build and Run

### Prerequisites
- Java Development Kit (JDK 17 or higher, tested on JDK 26)
- Windows PowerShell / Command Prompt or Unix terminal

### Option A: Using Windows Batch Scripts (Easiest)

1. **Compile:**
   ```cmd
   .\compile.bat
   ```
2. **Run Application:**
   ```cmd
   .\run.bat
   ```
3. **Run Automated Test Suite:**
   ```cmd
   .\test.bat
   ```

---

### Option B: Manual Command Line

1. **Compile:**
   ```bash
   javac -d bin src/com/parcel/exception/*.java src/com/parcel/strategy/*.java src/com/parcel/model/*.java src/com/parcel/service/*.java src/com/parcel/ui/*.java src/com/parcel/*.java
   ```

2. **Run Interactive Application:**
   ```bash
   java -cp bin com.parcel.Main
   ```

3. **Run Automated Unit & Domain Tests:**
   ```bash
   java -cp bin com.parcel.ParcelSystemTest
   ```

---

## 🧪 Automated Test Suite Coverage

The project includes `ParcelSystemTest.java`, testing:
- Tracking number regex validation (`ABC0001` to `ABC9999`).
- Document weight limits (≤ 0.5 kg) and page boundaries (1 to 100).
- Box volumetric calculations and integer ceil-rounding rule (2.0 -> 2, 2.1 -> 3).
- Fragile inheritance from `BoxParcel`, handling fee, and 30 kg weight limit.
- Verified test case against the prompt's exact worked example: `Php 330.00`.
- Standard, Express, and Same Day strategy calculations.
- Same Day > 10 kg restriction enforcing `ServiceNotAllowedException`.
- Singleton integrity across multiple calls.
- Duplicate tracking number, parcel not found, and already-delivered exception flows.

---

## 📦 Pushing to GitHub (`polymorphism-exception-patterns-exercise`)

To push this codebase to your GitHub repository:

1. Open your terminal in this directory:
   ```bash
   cd c:\Users\Heather\Heder
   ```

2. Initialize git and commit:
   ```bash
   git init
   git add .
   git commit -m "feat: complete parcel delivery system with polymorphism, exceptions, singleton and strategy patterns"
   ```

3. Create the remote repository on GitHub named:
   `polymorphism-exception-patterns-exercise`

4. Link the remote and push:
   ```bash
   git branch -M main
   git remote add origin https://github.com/<YOUR-GITHUB-USERNAME>/polymorphism-exception-patterns-exercise.git
   git push -u origin main
   ```

5. Copy your GitHub repository link:
   `https://github.com/<YOUR-GITHUB-USERNAME>/polymorphism-exception-patterns-exercise`
