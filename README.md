# 🛡️ England Technologies – Secure User Authentication, Vault, & Password Manager

A comprehensive JavaFX application providing secure user authentication, role-based access control, password management, and advanced file encryption. Designed for maximum privacy and usability, this system implements **AES-256 GCM encryption**, rotating access keys, and a secure file vault to protect your most sensitive data.

---

## 🔐 Key Features

### 🔑 User Authentication & Security

- **Advanced Password Management**
  - SHA-256 password hashing with random salt  
  - Password expiration and reuse prevention  
  - Password history enforcement

- **Multi-Level Login Verification**
  - Credentials validated securely against a MySQL backend  
  - Detection of expired, default, or disabled accounts  
  - Alert system for unusual login behavior

---

### 🔐 Secure Password Manager

- **Fully Encrypted Credential Storage**
  - Passwords, usernames, notes, and labels are stored encrypted using AES-256 GCM
  - Access to password entries is protected by account-level authentication and access key

- **On-Device Decryption**
  - Decryption only occurs locally, ensuring plaintext credentials are never transmitted

- **Strength Testing & Labeling**
  - Built-in password strength analysis tool  
  - Editable labels and notes for each entry

- **Secure Field-Level Storage**
  - Each password field is independently encrypted using unique IVs  
  - Prevents structural inference or brute-force correlation

---

### 🧰 File Vault

- **AES-256 GCM Encryption**
  - Full support for modern authenticated encryption  
  - Integrity-checked decryption prevents tampering  
  - Protection against CBC-based padding attacks

- **Filename Encryption**
  - Fully encrypted filenames and extensions  
  - No metadata leaks, even file type is hidden

- **Access Key System**
  - Access gated by per-user subscription key
  - Identity and access validated using public/private key system

- **Temporary File Protections**
  - Secure auto-deletion of temporary decrypted files  
  - Internal editor support to prevent data exposure

---

### 🧠 Smart File Handling

- File type auto-detection: Text, Image, PDF, Office, Binary  
- Built-in secure viewer/editor  
- Integration with default applications for external viewing  
- Encrypted file management within the vault

---

### 🧑‍💼 Role-Based Access Control

| User Type  | Capabilities                                                    |
|------------|----------------------------------------------------------------|
| SuperAdmin | Full system control: configs, vaults, users, logs              |
| Admin      | User and vault management, settings, log viewer                |
| Standard   | Basic vault and password manager access                        |
| Default    | Minimal access, primarily system setup                         |

- SuperAdmins can "impersonate" other users for support/debug but the vault and the Password Manager is strictly per-user accessible  
- Permissions tightly scoped by clearance level

---

## ⚙️ Technical Stack

- **Java 21** – Core language  
- **JavaFX** – Desktop GUI  
- **MySQL** – Encrypted backend storage  
- **Gradle** – Build & deployment automation  
- **AES-256 GCM** – Encryption layer  
- **JDBC** – Secure database communication  
- **Stripe** – Subscription processing

---

## 🔑 Access Keys & Subscription System

Vault and password manager access is gated by secure **subscription-based access keys**.

### 🛒 Purchase Access Keys

- Subscriptions are processed through Stripe
- Pricing:
  - **$2/month**
  - **$22/year** (discounted)
- Each user account is allowed **one access key**
- Once a valid subscription is active, the key is **automatically renewed and updated**
- Keys grant access to both the **vault system** and **password manager**

### 🧪 Trial Access

- New users may redeem **one 7-day free trial**  
- Trial keys are revoked automatically after expiration  
- Employers may also issue temporary keys via resume upload

### 🔄 Key Rotation & Management

- Public keys rotate per-login for forward secrecy  
- Private keys remain local for security  
- All access is validated server-side before vault or password access

---

## 🧾 Vault Architecture

- Encrypted vault folder separate from core app directory  
- Encrypted before disk write  
- No filenames or file types visible without decryption  
- Decryption creates temp copies only if explicitly requested  
- Automatic deletion on timeout, close, or logout

---

## 🧬 Security Philosophy

- No plaintext passwords stored or transmitted  
- Salted SHA-256 hashing to block rainbow tables  
- AES-256 GCM prevents tampering and ensures data authenticity  
- Zero-trust access key validation  
- Logging and alerting for security anomalies

---

## 🔗 Links

- 💼 [Purchase Access Key (via Stripe)](https://javaguidbhosting.alwaysdata.net/)
- 📝 [Submit Resume for Temporary Key](https://javaguidbhosting.alwaysdata.net/employer-resume)
- 🧪 Free trials available to new users

---

## ⚠️ Legal Notice

This software is proprietary to **England Technologies**.  
Unauthorized copying, redistribution, reverse engineering, or resale of access keys is strictly prohibited.  
All encryption logic, key systems, and platform designs are protected by copyright and applicable security laws.

© 2025 England Technologies – All Rights Reserved.
