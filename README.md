# ClipIt

ClipIt is a smart Android application designed to organize, track, and redeem detailed digital coupons and gift cards. Built with privacy and ease of use in use, it offers a complete solution for managing coupon expiry, remaining balances, and redemption history.

## Key Features

### 🏷️ Coupon Management
*   **Detailed Tracking**: Store coupons with names, expiration dates, initial values, and current balances.
*   **Organization**: Organize coupons into custom categories with personalized names, colors, and icons.
*   **Smart Search & Filter**: Easily find coupons by search query, category, or sorting by date, name, or value.
*   **Archiving**: Archive used or expired coupons to keep your main dashboard clean without losing data.

### 📱 Smart Redemption
*   **Instant Barcodes**: Automatically generates QR Codes, Code-128, and PDF-417 barcodes from redeem codes for easy scanning at checkout.
*   **Auto-Brightness**: Automatically maximizes screen brightness when displaying a redeem code to ensure successful scanning, restoring it afterwards.
*   **Partial Redemption**: Track spending by redeeming specific amounts from a coupon's total value.
*   **One-Time Use**: Support for single-use coupons that are automatically archived after redemption.

### 🧠 AI Integration (Gemini)
*   **Private by default**: incoming SMS are first checked on the phone. One-time passwords, bank alerts and delivery messages are never sent anywhere.
*   **Smart Parsing**: messages that look like coupons are parsed by Gemini (store, code, total value, expiry, redemption link) with a strict JSON schema, falling back to on-device regex if the AI is unavailable.
*   **Bring your own key**: requests go directly from your phone to Google's Gemini API with your own API key. Pick any model your key can use; the list is loaded live from the API.

### ☁️ Cloud & Local Backup
*   **Google Drive Sync**: Seamlessly backup your entire database to Google Drive and restore it on any device.
*   **Manual Export**: Export your database as a portable file for manual backups.
*   **Privacy Focused**: Your data belongs to you. No external servers are used other than your own Google Drive.

### 📜 History & Time Travel
The app maintains an immutable timeline of every action performed on a coupon, serving as an audit trail and safety net.
*   **Tracked Operations**:
    *   **Creation**: Initial entry.
    *   **Edits**: Changes to name, date, category, etc.
    *   **Usage**: Deductions from balance.
    *   **Status**: Archiving/Unarchiving.
*   **Restore Capability**: Revert a coupon to *any* previous state in its history. This is a non-destructive action that adds a "Restored" entry to the timeline, preserving the history chain.

## Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Database**: Room (SQLite)
*   **Architecture**: MVVM with Unidirectional Data Flow
*   **AI**: Google Gemini API (`google-generativeai`)
*   **Cloud**: Google Drive API for Android
*   **Barcode Generation**: ZXing Library
*   **Concurrency**: Kotlin Coroutines & Flow

## Setup & usage

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/nimrodts/ClipIt.git
    ```
2.  **Open in Android Studio**:
    Open the project in the latest version of Android Studio Koala or later.
3.  **Build & Run**:
    Connect an Android device or start an emulator and run the `app` configuration.

### Configuring AI Features
To use the AI parsing features, you need a Google Gemini API Key:
1.  Get an API key from [Google AI Studio](https://aistudio.google.com/).
2.  In the app, go to **Settings** -> **AI Settings**.
3.  Enter your API Key and tap **Save Key**.

### Configuring Backups
1.  Go to **Settings** -> **Database Settings**.
2.  Tap "Connect Google Drive" and assign your google account.
3.  You can now upload and restore backups.

## License

This project is open source.
