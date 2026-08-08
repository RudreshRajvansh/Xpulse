# XPulse

Android app for XPulse, a consent-first personal health record built on ABDM rails.

Kotlin and Jetpack Compose with Hilt, following a clean domain / data / presentation split. Covers eight roles — patient, doctor, receptionist, pharmacy, diagnostics, admin, super admin and customer care — with time-bound consent sharing, medicine reminders, on-device prescription OCR and QR check-in.

Talks to the [XPulse backend](https://github.com/RudreshRajvansh/Xpulse-Backend). The server address is editable from the login screen, so no rebuild is needed to repoint it.

Open in Android Studio and run, or `./gradlew assembleDebug`.
