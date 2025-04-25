# GUI

This repository contains a Java-based graphical user interface (GUI) application. The project focuses on user authentication, password management, logging, and system configuration. It includes features for secure credential validation and user session management, making it well-suited for applications requiring robust security measures.

## Features

- **Password Management**:
  - Password hashing using SHA-256 and salt for enhanced security.
  - Password history verification to prevent reuse of old passwords.
  - Generation of salted and unsalted hashed passwords.

- **User Authentication**:
  - Credential validation against a database using hashed passwords.
  - Default account usage check and notification system.
  
- **Logging and System Management**:
  - Custom logging framework with formatted messages and timestamps.
  - Log clearing with warnings for unauthorized actions.

- **System Configuration**:
  - File and directory management with support for calculating directory sizes.
  - User status management within the system.

## Code Overview

### Key Classes

1. **PasswordHasher**:
   - Provides methods for hashing passwords with or without salt.
   - Retrieves and manages salts for users.
   - Implements secure random salt generation.

2. **DefaultAccountChecker**:
   - Manages the detection and notification of default account usage.

3. **CredentialChecker**:
   - Validates user credentials against a database.

4. **LogClearer**:
   - Handles log clearing operations and enforces security checks for unauthorized actions.

5. **User**:
   - Represents a user in the system, including properties like username, group, and status.
   - Allows updates to user status and group assignments.

6. **ManageFiles**:
   - Manages file and directory operations, including calculating directory sizes.

7. **Logger**:
   - Custom logger with formatted messages and custom symbols for information and warnings.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or later.
- A relational database for storing user credentials and logs.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/awesomeshot5051/GUI.git
   cd GUI
   ```

2. Open the project in your preferred Java IDE (e.g., IntelliJ IDEA or Eclipse).

3. Configure the database connection in the `Main` class or session manager.

4. Compile and run the application:
   ```bash
   javac Main.java
   java Main
   ```

## Usage

- Launch the application and interact with the GUI to manage users, passwords, and logs.
- Use the provided logging framework for debugging and monitoring application events.

## Contributing

Contributions are welcome! Please fork the repository, create a new branch, and submit a pull request with your changes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Databasse Backend

At the moment, this is using [alwaysdata.net](https://admin.alwaysdata.com/) as the backend database. This hosts everyone's username and encrypted password, as well as what kind of group your are in.
