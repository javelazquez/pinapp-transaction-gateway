# PinApp Transaction Gateway

## Overview
PinApp Transaction Gateway is a robust and scalable service designed to manage and process financial transactions. Built with modern Java practices and following the **Hexagonal Architecture** (Ports and Adapters) pattern, it ensures a clean separation of concerns and high maintainability.

## Technology Stack
- **Java**: 21 (LTS)
- **Framework**: Spring Boot 3.4.1
- **Build Tool**: Maven
- **Architecture**: Hexagonal Architecture
- **SDK Integration**: Integrated with `pinapp-notify-sdk` for transaction notifications.

## Project Structure
The project follows the Hexagonal Architecture pattern:
- `com.pinapp.gateway.domain`: Core business logic and models. Independent of any framework.
- `com.pinapp.gateway.application`: Use cases and application services (Inbound Ports implementation).
- `com.pinapp.gateway.infrastructure`: Adapters for external systems (REST controllers, SDK integration, database persistence).

## Key Features
- **Transaction Processing**: Securely processes inbound transaction requests.
- **Notification Integration**: Automatically notifies external systems via the PinApp Notify SDK.
- **Status Management**: Tracks the lifecycle of a transaction (PENDING, APPROVED, REJECTED, etc.).

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.9+
- Access to `pinapp-notify-sdk` (ensure it is installed in your local maven repository)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/pinapp/pinapp-transaction-gateway.git
   cd pinapp-transaction-gateway
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Reference

### Process Transaction
- **URL**: `/api/v1/transactions`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "amount": 1000.50,
    "currency": "USD",
    "description": "Payment for services",
    "metadata": {
      "customer_id": "cust_123"
    }
  }
  ```
- **Success Response**: `200 OK` with transaction details.

## Author
Developed by the **PinApp Engineering Team**.
