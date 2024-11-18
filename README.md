# RISC-V Emulator

RiscV emulator designed in java

# Project Overview

This project is a Java-based emulator designed to simulate the behavior of a RISC-V processor, providing users with an
environment to run and test RISC-V programs. The emulator serves as a versatile tool for learning, testing, and
debugging RISC-V code by replicating core RISC-V functionalities.

## Features

- **Instruction Set Simulation**: Supports a subset of the RISC-V instruction set.
- **Memory Management**: Simulates memory operations and management.
- **Debugging Tools**: Provides tools for debugging and inspecting the state of the emulator.

## Requirements

- **Java**: Ensure you have Java installed on your system.
- **Maven**: This project uses Maven for dependency management and build automation.

## Setup

1. Clone the repository:
   ```sh
   git clone https://github.com/gabrielf4ustino/emulator.git
   ```

2. Navigate to the project directory:
   ```sh
   cd emulator
    ```

3. Build the project using Maven:
   ```sh
   mvn clean install
    ```

## Usage

1. Run the emulator:
   ```sh
   java -jar target/emulator-1.0-SNAPSHOT.jar
    ```