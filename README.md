# 🔐 LifeVault – Your Digital Life, Safely Preserved

**LifeVault** is an Android-based digital life management application designed to securely organize important personal information, medical records, documents, emergency contacts, and digital assets in one place.

It helps users manage essential information, access emergency details quickly, and securely preserve important data for trusted nominees.

---

## 📌 Project Overview

Managing personal documents, medical information, emergency contacts, and digital assets across multiple platforms can be difficult.

LifeVault addresses this problem through a centralized application that combines secure data management, emergency assistance, and digital legacy features.

## ✨ Key Features

- **Secure Vault:** Store and manage important personal information securely.
- **Emergency Profile:** Maintain essential medical and emergency information.
- **SOS Assistance:** Access emergency assistance features quickly.
- **QR Emergency Card:** Provide quick access to emergency information through a QR code.
- **Medical Records:** Organize and manage medical documents.
- **Medicine Reminders:** Manage medication schedules and reminders.
- **Trusted Contacts:** Maintain important emergency contacts.
- **Digital Legacy:** Support the preservation and transfer of important digital information.
- **Nominee Access:** Provide nominee-related access functionality.
- **AI Assistant:** AI-powered assistance integrated into the application.
- **Document Management:** Organize important personal documents.
- **Authentication:** Secure user registration and login.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| Jetpack Compose | Modern declarative UI |
| MVVM Architecture | Separation of UI and business logic |
| Clean Architecture | Modular and maintainable code structure |
| Firebase Authentication | User authentication |
| Cloud Firestore | Cloud-based data management |
| Room | Local database management |
| DataStore | Local preference storage |
| Firebase | Backend services |
| Gemini API | AI assistant integration |
| Gradle | Build automation |

## 🏗️ Project Architecture

LifeVault follows **MVVM and Clean Architecture principles** to separate application responsibilities.

```text
LifeVault
│
├── Android-App
│   ├── app
│   │   └── src/main
│   │       ├── java/com/lifevault/app
│   │       │   ├── data
│   │       │   │   ├── ai
│   │       │   │   ├── crypto
│   │       │   │   ├── notification
│   │       │   │   ├── preferences
│   │       │   │   ├── repository
│   │       │   │   └── sos
│   │       │   │
│   │       │   ├── di
│   │       │   ├── domain
│   │       │   │   ├── model
│   │       │   │   └── repository
│   │       │   │
│   │       │   └── presentation
│   │       │       ├── auth
│   │       │       ├── dashboard
│   │       │       ├── documents
│   │       │       ├── emergencyprofile
│   │       │       ├── medicinereminder
│   │       │       ├── nominee
│   │       │       ├── passwordvault
│   │       │       ├── qrcard
│   │       │       ├── securevault
│   │       │       ├── sos
│   │       │       └── trustedcontacts
│   │       │
│   │       └── res
│   │
│   └── Gradle Configuration
│
├── Admin
│   ├── admin.html
│   └── index.html
│
├── README.md
└── LifeVault_Project_Report.docx (4).pdf
```

### Architecture Layers

- **Presentation Layer:** Handles UI screens, user interactions, and ViewModels.
- **Domain Layer:** Contains core application models and repository contracts.
- **Data Layer:** Handles data operations, authentication, encryption, preferences, and external service integrations.
- **Dependency Injection:** Organizes and provides application dependencies.

## 🖥️ Admin Panel

LifeVault also includes a web-based administration interface.

The Admin folder contains:
- `admin.html`
- `index.html`

These files provide the web interface for administrative operations and Firebase-connected functionality.

## ⚙️ Getting Started

### Prerequisites

- Android Studio
- JDK compatible with the project's Gradle configuration
- Android SDK
- Firebase project
- Gemini API key (for AI assistant functionality)

### 1. Clone the Repository

```bash
git clone https://github.com/thejaswinisaiml24-source/LIFE-VAULT.git
```

### 2. Open the Android Project

Open the `Android-App` folder in Android Studio.

Allow Gradle to sync and download the required dependencies.

### 3. Configure Firebase

Obtain your Firebase Android configuration file from Firebase Console.

Place `google-services.json` inside:

```text
Android-App/app/
```

This file is intentionally excluded from version control.

### 4. Configure Gemini API Key

Create or update `local.properties` inside the `Android-App` folder and add:

```properties
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

Replace the placeholder with your own API key.

Do not commit real API keys or private credentials.

### 5. Run the Application

Connect an Android device or start an emulator, then build and run the application through Android Studio.

## 🔒 Security Considerations

- Sensitive configuration files are excluded from Git.
- API keys should be stored outside tracked source files.
- Authentication and database access should be protected by appropriate Firebase security rules.
- Sensitive data should be handled using secure storage and encryption practices.

## 📂 Repository Structure

| Folder/File | Description |
|---|---|
| `Android-App/` | Android application source code |
| `Admin/` | Web-based administration interface |
| `README.md` | Project documentation |
| `LifeVault_Project_Report.docx (4).pdf` | Project report |

## 🚀 Future Enhancements

- Improved emergency response integrations.
- Enhanced biometric security.
- Additional digital legacy management capabilities.
- More comprehensive security monitoring.
- Improved user experience and accessibility.

## 👩‍💻 Project

**LifeVault – Digital Life Management and Emergency Assistance System**

Developed as an academic project.

**Repository:**  
https://github.com/thejaswinisaiml24-source/LIFE-VAULT

