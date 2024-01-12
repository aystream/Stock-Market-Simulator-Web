# Stock Market Simulator Web

## Overview

This application simulates a stock market environment with a matching engine, order books, trade ledger, and a trading gateway. Clients can interact with the market using a REST API for order entry and a WebSocket API for real-time trade updates.

## Project Structure

- **api/**: REST API interfaces and WebSocket communication for the Trading Gateway.
- **bootstrap/**: Runner layer responsible for bootstrapping and launching the application.
- **cli/**: Command Line Interface for administrative tasks and market simulation control.
- **core/**: Core business logic, domain models, and repositories.
    - **exception/**: Custom exception handling and logic.
    - **matching/**: Logic for the Matching Engine.
    - **orderbook/**: Management of Order Books for different stock symbols.
    - **trade/**: Trade Ledger logic to record all executed trades.

## Getting Started

### Prerequisites

- Java JDK 17 or later
- Gradle 8.5 or later 

### Running the Application

1. To start the application, navigate to the project root and run:
  
   ```sh
    ./gradlew bootRun
   ```
   or
   ```sh
    ./gradlew :bootstrap:bootRun
   ```

2. To start the CLI application, navigate to the cli module and run:
   
   ```sh
    ./gradlew bootCLI
   ```
   or
   ```sh
    ./gradlew :cli:bootRun
   ```
   The CLI will start and be ready to accept commands.
### CLI Commands

- `add [symbol] [type] [quantity] [price]`: Adds a new order.
- `cancel [orderId]`: Cancels an existing order.
- `view [symbol]`: Views orders for a symbol.
- `exit`: Exits the CLI application.   

## Testing

To run tests and ensure the application passes all test cases:

```sh
./gradlew test
```

Look for the `BUILD SUCCESSFUL` message to confirm that all tests have passed.

## Configuration

Configuration files are located in the `src/main/resources` directory of each module. Customize the application settings by modifying `application.yml`.

## Additional Information on Project Scope and Limitations

This project was developed with a focus on simplicity and functionality. As such, certain aspects and features might not have been fully developed or included in the project scope. These include, but are not limited to:

- **Creation of Separate DTO Models**: The project currently lacks distinct Data Transfer Objects (DTOs), which are typically used for encapsulating data and separating the external interface from the internal database models.

- **Externalized Configuration Variables**: Configuration variables and settings are not fully externalized. Ideally, these should be placed in customizable configuration files for easier management and adaptability.

- **Comprehensive Lifecycle Management**: The project does not implement a full-fledged lifecycle management for various components. A more comprehensive approach would enhance the robustness and scalability of the system.

- **Enhanced Interface Design for Agility**: The project could benefit from more interfaces to abstract the implementation details. This would allow for greater flexibility and easier adaptability, aligning with agile development practices.

- **Selective Testing**: Tests have been written primarily for critical modules and in areas where challenges were encountered during debugging. Not all components of the system have comprehensive test coverage, reflecting the project's prioritization of key functionalities.

It's important to note that these limitations are reflective of the project's current state and do not detract from its core functionality. The system is designed to be open for further enhancements and refinements in these areas.
