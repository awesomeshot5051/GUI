# 🛡️ England Technologies – Secure User Authentication & Encryption System

A comprehensive JavaFX application designed to provide secure user authentication, role-based access control, and advanced file encryption. This system employs **AES-256 GCM encryption**, secure access keys, and a professional file vault system to ensure your data is protected with military-grade security practices.

---

## 🔐 Key Features

### 🔑 User Authentication & Security

- **Advanced Password Management**  
  - SHA-256 password hashing with random salt  
  - Password expiration and reuse prevention  
  - Password history enforcement

- **Multi-Level Verification**  
  - Credentials securely validated against a MySQL backend  
  - Detection of default, expired, or disabled accounts  
  - Alert system for unusual login behavior

### 🧰 Secure File Vault

- **Modern AES-256 GCM Encryption**  
  - Uses AES/GCM/NoPadding — a secure, authenticated encryption mode  
  - Protects both file contents and integrity against tampering  
  - Eliminates vulnerability to padding oracle attacks (unlike CBC mode)

- **File Vault Access Key System**  
  - Vault access is gated by secure, rotating access keys  
  - Public/private key verification ensures identity and access control  
  - Permanent keys (paid licenses) and temporary employer-issued keys (via resume verification)

- **Secure Filename Encryption**  
  - Filenames are encrypted using a separate cipher process  
  - File extensions are hidden to prevent metadata leaks  
  - Even file types cannot be guessed without access

### 🧠 Smart File Handling

- File type auto-detection (Text, Image, PDF, Office, Binary)  
- Built-in text viewer/editor  
- External viewer integration for complex formats  
- Temporary decrypted files are securely wiped after use

### 🧑‍💼 Role-Based Access Control

| User Type  | Capabilities                                                    |
|------------|----------------------------------------------------------------|
| SuperAdmin | Full control over system configuration, database, users, vault, and logging |
| Admin      | User and vault management, log access, personal settings       |
| Standard   | Basic vault and personal access                                 |
| Default    | Minimal access, used for system initialization                  |

- SuperAdmin can switch into other user accounts for testing or support  
- All roles access only what is explicitly granted based on clearance level

### 🗂️ File & Directory Management

- Full directory explorer with file/folder tools  
- Built-in storage optimization and cleanup utilities  
- Vault integration with user-specific access

### 🖥️ User Interface & Experience

- Built with JavaFX for a modern, desktop-grade interface  
- Clean login and onboarding flow  
- Role-specific dashboards with dynamic controls  
- Alerts and tooltips to guide secure usage

---

## ⚙️ Technical Stack

- **Java 21** – Core application  
- **JavaFX** – UI rendering  
- **MySQL** – Remote database via alwaysdata.net  
- **Gradle** – Build and deployment automation  
- **JCE (Java Cryptography Extension)** – Cryptography backend  
- **AES-256 GCM** – File encryption (with random IV and 128-bit authentication tag)  
- **JDBC** – Secure database connections

---

## 🧾 File Vault Access Workflow

### 🔑 Permanent Keys

- Purchased via the secure payment portal  
- Stored in a MySQL table  
- Permanently linked to a user account

### 🔓 Temporary Keys

- Issued when employers upload your resume  
- Valid for 7 days  
- Automatically expire and are revoked

### 🔐 Key Rotation and Verification

- Public keys are generated each time you login  
- Keys rotate periodically for forward secrecy  
- Private key is stored locally for decryption only

---

## 🔄 Vault Architecture

- All files encrypted before disk write  
- Vault path is isolated from application directories  
- Encrypted files cannot be identified without a valid key  
- Internal editors/viewers protect against temp file abuse  
- Automatic deletion of decrypted copies on close or timeout

---

## 🧬 Security Philosophy

- Zero plaintext password storage  
- Salted hash protection from rainbow table attacks  
- AES-256 GCM encryption instead of insecure CBC  
- Session tracking and last login logging  
- Logging of suspicious activity  
- Access key validation before decryption

---

## Access Keys
- You can purchase the access keys using this (link)[https://javaguidbhosting.alwaysdata.net/]
- Employers can redeem a temporary access key by uploading a copy of my resume.
- You can redeem one free trial for the vault, which lasts 7 days.
- All temporary access keys are valid for 7 days and will automatically be deleted past that time period
- Peremant access keys are $2

## ⚠️ Legal Notice

This software is proprietary to England Technologies. Unauthorized use, reproduction, reverse engineering, or distribution is strictly prohibited. All encryption logic, key management algorithms, and access validation mechanisms are protected under copyright.

© 2025 England Technologies – All Rights Reserved.
