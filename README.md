# My Application

An Android application built with Kotlin that demonstrates modern Android development practices, featuring user management functionality, Google AdMob integration, Firebase services, and in-app purchases.

## 📱 Features

- **User Management**: Complete CRUD operations for user data
  - Create, Read, Update, and Delete users
  - Search functionality
  - Local data persistence using Room database

- **Ad Monetization**: Integrated Google AdMob ads
  - App Open Ads
  - Banner Ads
  - Interstitial Ads
  - Native Ads
  - User Messaging Platform (UMP) for consent management

- **Firebase Integration**
  - Firebase Analytics
  - Firebase Remote Config for dynamic ad configuration

- **In-App Purchases**: Google Play Billing integration for premium features

- **Modern Architecture**: Clean Architecture with MVVM pattern

## 🏗️ Project Structure

The project is organized into multiple modules:

```
MyApplication/
├── app/                    # Main application module
│   ├── src/main/
│   │   ├── java/com/mobile/test/application/
│   │   │   ├── app/        # Application class
│   │   │   ├── core/       # Core utilities and extensions
│   │   │   ├── data/       # Data layer (Room, Repository implementation)
│   │   │   ├── di/         # Dependency injection modules
│   │   │   ├── domain/     # Domain layer (models, use cases, repository interfaces)
│   │   │   └── presentation/ # Presentation layer (ViewModels, Views, Adapters)
│   │   └── res/            # Resources (layouts, strings, etc.)
│
├── AdsModule/              # AdMob ads module
│   └── src/main/           # Ad management and display logic
│
└── RemoteConfig/           # Firebase Remote Config module
    └── src/main/           # Remote configuration management
```

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin** - Primary programming language
- **Android SDK** - Target SDK 36, Min SDK 26
- **Gradle** - Build system with Kotlin DSL

### Architecture & Design Patterns
- **Clean Architecture** - Separation of concerns across layers
- **MVVM (Model-View-ViewModel)** - Presentation pattern
- **Repository Pattern** - Data abstraction layer

### Libraries & Frameworks

#### Dependency Injection
- **Hilt** (2.57.1) - Dependency injection framework

#### Database
- **Room** (2.8.0) - Local database persistence
- **KSP** (2.0.21-1.0.27) - Kotlin Symbol Processing

#### UI & Navigation
- **Material Design Components** (1.13.0)
- **Navigation Component** (2.9.6) - Fragment navigation
- **ViewBinding** - Type-safe view references
- **SDP** (1.1.1) - Scalable size units

#### Asynchronous Operations
- **Kotlin Coroutines** (1.8.0) - Asynchronous programming
- **Lifecycle Components** (2.8.7) - Lifecycle-aware components

#### Firebase
- **Firebase BOM** (34.3.0)
  - Firebase Analytics
  - Firebase Remote Config

#### Ads & Monetization
- **Google Play Services Ads** (24.4.0) - AdMob integration
- **User Messaging Platform** (4.0.0) - Consent management
- **Google Play Billing** (7.1.1) - In-app purchases

#### Utilities
- **Gson** (2.13.2) - JSON serialization/deserialization

## 📋 Prerequisites

- **Android Studio** - Hedgehog or later recommended
- **JDK 11** or higher
- **Android SDK** with API level 26+ (Android 8.0)
- **Google AdMob Account** - For ad unit IDs
- **Firebase Project** - For Firebase services
- **Google Play Console Account** - For in-app purchases (optional)

## 📱 Modules

### App Module
The main application module containing:
- User management features
- Navigation setup
- Dependency injection configuration
- Main activity and fragments

### AdsModule
A reusable library module for ad management:
- Ad loading and display logic
- Ad state management
- Banner, Interstitial, Native, and App Open ad implementations

### RemoteConfig Module
Firebase Remote Config integration:
- Remote configuration management
- Ad unit ID configuration
- Dynamic feature flags

## 🏛️ Architecture

The app follows **Clean Architecture** principles:

```
┌─────────────────────────────────────┐
│      Presentation Layer             │
│  (Fragments, ViewModels, Adapters)  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Domain Layer                │
│  (Use Cases, Models, Interfaces)    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Data Layer                 │
│  (Repository, Room, Data Sources)   │
└─────────────────────────────────────┘
```

### Key Components

- **Domain Layer**: Business logic and use cases
- **Data Layer**: Repository implementations, Room database, data mappers
- **Presentation Layer**: UI components, ViewModels, state management
- **DI Layer**: Hilt modules for dependency injection

## 🔧 Configuration

### ProGuard Rules

ProGuard rules are configured in:
- `app/proguard-rules.pro`
- `AdsModule/proguard-rules.pro`
- `RemoteConfig/proguard-rules.pro`

### Version Catalog

Dependency versions are managed in `gradle/libs.versions.toml` using Gradle Version Catalog.

## 📝 Notes

- The app uses test ad unit IDs by default. Replace them with your production ad unit IDs before publishing.
- Ensure you have proper AdMob and Firebase configurations before building for production.
- The app includes consent management for GDPR and CCPA compliance.

## 🔗 Resources

- [Android Developer Documentation](https://developer.android.com/)
- [AdMob Documentation](https://developers.google.com/admob/android)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)

---

**Note**: This is a sample application demonstrating modern Android development practices. Update configuration files with your own credentials before deploying to production.

