# Secure User Authentication & Encryption System

A comprehensive JavaFX application providing secure user authentication, role-based access control, and advanced file
encryption. This system features robust security measures including password hashing, account status management,
detailed logging functionality, and secure file vault capabilities.

## Features

### User Authentication & Security

- **Advanced Password Management**:
    - SHA-256 hashing with salt for enhanced security
    - Password expiration enforcement
    - Password history verification to prevent reuse
- **Multi-level User Validation**:
    - Credential verification against secure database
    - Account status checking (enabled/disabled)
    - Default account detection and notification

### Secure File Vault

- **File Encryption Vault**:
    - AES encryption with CBC mode and PKCS5Padding
    - Secure storage of sensitive files in an encrypted vault
    - Automatic file type detection and appropriate handling
    - Both filename and content encryption for maximum security

- **Smart File Handling**:
    - Automatic detection of file types (text, image, PDF, office documents, binary)
    - Specialized viewers for different file formats
    - Internal text editor for quick modifications
    - External application integration for complex file types

- **Key Management**:
    - Public/private key verification before encryption/decryption
    - Secure key rotation for enhanced security
    - Key verification to prevent unauthorized access

### Role-Based Access Control

- **User Types and Permissions**:
    - **SuperAdmin**: Full system access with database management, system configuration, and user administration
      privileges
    - **Admin**: Advanced management capabilities including user creation, vault access, and log viewing
    - **Standard**: Basic functionality with limited management capabilities
    - **Default**: Initial account type with minimal privileges

- **User Management Dashboard**:
    - Complete system management interface
    - Access to vault management functionality
    - User switching capabilities for troubleshooting (SuperAdmin only)

### File and Directory Management

- **Manage Files and Folders**:
    - Comprehensive file system explorer
    - Directory size calculation and analysis
    - File organization and cleanup tools
    - Storage space optimization
    - Directory navigation and file management within the application

### System Management

- **Comprehensive Logging**:
    - Detailed event tracking with timestamps
    - Login attempts (successful and failed)
    - Security-related warnings
- **Session Management**:
    - Secure user sessions with proper authentication
    - Last login date tracking
    - Session information persistence

### User Interface

- **JavaFX-based GUI**:
    - Clean login interface
    - Role-appropriate dashboards
    - Alert system for notifications
    - File vault browsing and management interface
- **Dynamic UI Elements**:
    - Context-sensitive screens based on user permissions
    - Intuitive navigation
    - Specialized file viewing and editing capabilities

## User Type Capabilities

### SuperAdmin

- Full system configuration access
- Database management and execution
- User management (create, modify, disable)
- Password expiration settings
- Complete access to logs
- File vault management
- File and folder management
- Can switch to other user accounts for troubleshooting

### Admin

- User management (create and modify)
- Log viewing capabilities
- File vault access
- File and folder management
- Personal settings modification

### Standard

- Basic application functionality
- Personal settings modification
- Limited file management capabilities

### Default

- Initial account used for first-time setup
- Automatically enabled when system detects first-use

## Technical Details

### Technology Stack

- **Java 21**: Core application language
- **JavaFX**: UI framework
- **MySQL**: Backend database for user data
- **JDBC**: Database connectivity
- **Gradle**: Build system
- **Java Cryptography Extensions (JCE)**: Encryption implementation

### Security Implementation

- Passwords are never stored in plain text
- Salted hashing prevents rainbow table attacks
- Account locking for security breaches
- Password expiration policies
- AES-256 encryption for file contents
- Secure temporary file handling with automatic cleanup

### File Vault Architecture

- **Secure File Management**:
    - Files stored with encrypted names and contents
    - Dedicated vault directory separate from application files
    - Type-specific handling for different file formats
    - Internal viewer/editor for supported formats

- **Temporary File Safety**:
    - Secure creation and deletion of temporary files
    - Automatic cleanup on application exit
    - Protected viewing and editing workflow

## Database Configuration

The application connects to a MySQL database hosted on alwaysdata.net:

- Database URL: jdbc:mysql://mysql-javaguidbhosting.alwaysdata.net:3306/javaguidbhosting_userdatabase
- Username credentials are managed through the application
- Database purges occur periodically based on last login date

**Security Note**: When creating accounts, use unique credentials that aren't used for other important services.

## Using the File Vault

The file vault system provides a secure way to store sensitive files:

1. Access the vault through the "Vault" button in the dashboard (Admin and SuperAdmin access)
2. View encrypted files with appropriate viewers based on file type
3. Edit text files directly within the application
4. Open and edit other file types using your system's default applications
5. All files are automatically re-encrypted after editing

## Legal Notice

This software is proprietary and all rights are reserved. Unauthorized copying, modification, distribution, or use of
this software is strictly prohibited. This software is provided for authorized use only.

© 2024 All Rights Reserved